/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;
import org.ballerinalang.debugadapter.config.ClientConfigHolder;
import org.ballerinalang.debugadapter.config.ClientLaunchConfigHolder;
import org.ballerinalang.debugadapter.evaluation.ExpressionEvaluator;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.ballerinalang.debugadapter.jdi.ThreadReferenceProxyImpl;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.utils.PackageUtils.BAL_FILE_EXT;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getQualifiedClassName;
import static org.eclipse.lsp4j.debug.OutputEventArgumentsCategory.STDOUT;

/**
 * JDI Event processor implementation.
 */
public class JDIEventProcessor {

    private final ExecutionContext context;
    private boolean isRemoteVmAttached = false;
    private final Map<String, Map<Integer, BalBreakpoint>> breakpoints = new HashMap<>();
    private final List<EventRequest> stepEventRequests = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(JBallerinaDebugServer.class);
    private static final String BALLERINA_ORG_PREFIX = "ballerina";
    private static final String BALLERINAX_ORG_PREFIX = "ballerinax";
    private static final String CONDITION_TRUE = "true";

    JDIEventProcessor(ExecutionContext context) {
        this.context = context;
    }

    /**
     * Asynchronously listens and processes the incoming JDI events.
     */
    void startListening() {
        CompletableFuture.runAsync(() -> {
            isRemoteVmAttached = true;
            while (isRemoteVmAttached) {
                try {
                    EventSet eventSet = context.getDebuggeeVM().eventQueue().remove();
                    EventIterator eventIterator = eventSet.eventIterator();
                    while (eventIterator.hasNext() && isRemoteVmAttached) {
                        processEvent(eventSet, eventIterator.next());
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            // Tries terminating the debug server, only if there is no any termination requests received from the
            // debug client.
            if (!context.getAdapter().isTerminationRequestReceived()) {
                // It is not required to terminate the debuggee (remote VM) in here, since it must be disconnected or
                // dead by now.
                context.getAdapter().terminateServer(false);
            }
        });
    }

    private void processEvent(EventSet eventSet, Event event) {
        if (event instanceof ClassPrepareEvent) {
            if (context.getLastInstruction() != DebugInstruction.STEP_OVER) {
                ClassPrepareEvent evt = (ClassPrepareEvent) event;
                configureUserBreakPoints(evt.referenceType());
            }
            eventSet.resume();
        } else if (event instanceof BreakpointEvent) {
            BreakpointEvent bpEvent = (BreakpointEvent) event;
            ReferenceType bpReference = bpEvent.location().declaringType();
            String qualifiedClassName = getQualifiedClassName(context, bpReference);
            Map<Integer, BalBreakpoint> breakpoints = this.breakpoints.get(qualifiedClassName);
            int lineNumber = bpEvent.location().lineNumber();

            if (breakpoints != null && breakpoints.containsKey(lineNumber)
                    && breakpoints.get(lineNumber).getCondition() != null
                    && !evaluateBreakpointCondition(breakpoints.get(lineNumber).getCondition(), bpEvent.thread())) {
                context.getDebuggeeVM().resume();
            } else {
                notifyStopEvent(event);
            }
        } else if (event instanceof StepEvent) {
            StepEvent stepEvent = (StepEvent) event;
            long threadId = stepEvent.thread().uniqueID();
            if (isBallerinaSource(stepEvent.location())) {
                if (isExternalLibSource(stepEvent.location())) {
                    // If the current step-in event is related to an external source (i.e. lang library, standard
                    // library, connector module), notifies the user and rolls back to the previous state.
                    // Todo - add support for external libraries
                    context.getAdapter().sendOutput("INFO: Stepping into ballerina internal modules " +
                            "is not supported.", STDOUT);
                    context.getAdapter().stepOut(threadId);
                    return;
                }
                // If the current step event is related to a ballerina source, suspends all threads and notifies the
                // client that the debuggee is stopped.
                notifyStopEvent(event);
            } else {
                int stepType = ((StepRequest) event.request()).depth();
                sendStepRequest(threadId, stepType);
            }
        } else if (event instanceof VMDisconnectEvent
                || event instanceof VMDeathEvent
                || event instanceof VMDisconnectedException) {
            isRemoteVmAttached = false;
        } else {
            eventSet.resume();
        }
    }

    void setBreakpoints(String path, Map<Integer, BalBreakpoint> breakpoints) {
        this.breakpoints.put(getQualifiedClassName(path), breakpoints);
        if (context.getDebuggeeVM() != null) {
            // Setting breakpoints to a already running debug session.
            context.getEventManager().deleteAllBreakpoints();
            context.getDebuggeeVM().allClasses().forEach(this::configureUserBreakPoints);
        }
    }

    void sendStepRequest(long threadId, int stepType) {
        if (stepType == StepRequest.STEP_OVER) {
            configureDynamicBreakPoints(threadId);
        } else if (stepType == StepRequest.STEP_INTO || stepType == StepRequest.STEP_OUT) {
            createStepRequest(threadId, stepType);
        }
        context.getDebuggeeVM().resume();
        // Notifies the debug client that the execution is resumed.
        ContinuedEventArguments continuedEventArguments = new ContinuedEventArguments();
        continuedEventArguments.setAllThreadsContinued(true);
        context.getClient().continued(continuedEventArguments);
    }

    void restoreBreakpoints(DebugInstruction instruction) {
        if (context.getDebuggeeVM() == null) {
            return;
        }

        context.getEventManager().deleteAllBreakpoints();
        if (instruction == DebugInstruction.CONTINUE) {
            context.getDebuggeeVM().allClasses().forEach(this::configureUserBreakPoints);
        }
    }

    private void configureUserBreakPoints(ReferenceType referenceType) {
        try {
            // Avoids setting break points if the server is running in 'no-debug' mode.
            ClientConfigHolder configHolder = context.getAdapter().getClientConfigHolder();
            if (configHolder instanceof ClientLaunchConfigHolder
                    && ((ClientLaunchConfigHolder) configHolder).isNoDebugMode()) {
                return;
            }

            String qualifiedClassName = getQualifiedClassName(context, referenceType);
            if (!breakpoints.containsKey(qualifiedClassName)) {
                return;
            }
            Map<Integer, BalBreakpoint> breakpoints = this.breakpoints.get(qualifiedClassName);
            for (BalBreakpoint bp : breakpoints.values()) {
                List<Location> locations = referenceType.locationsOfLine(bp.getLine().intValue());
                if (!locations.isEmpty()) {
                    Location loc = locations.get(0);
                    BreakpointRequest bpReq = context.getEventManager().createBreakpointRequest(loc);
                    bpReq.enable();
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void configureDynamicBreakPoints(long threadId) {
        ThreadReferenceProxyImpl threadReference = context.getAdapter().getAllThreads().get(threadId);
        try {
            List<StackFrameProxyImpl> jStackFrames = threadReference.frames();
            List<StackFrame> balStackFrames = jStackFrames.stream().map(stackFrameProxy -> {
                try {
                    StackFrame stackFrame = stackFrameProxy.getStackFrame();
                    return JBallerinaDebugServer.isBalStackFrame(stackFrame) ? stackFrame : null;
                } catch (JdiProxyException e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            if (!balStackFrames.isEmpty()) {
                configureBreakpointsForMethod(threadId, balStackFrames.get(0));
            }
            // If the current function is invoked within another ballerina function, we need to explicitly set another
            // temporary breakpoint on the location of its invocation. This is supposed to handle the situations where
            // the user wants to step over on an exit point of the current function.
            if (balStackFrames.size() > 1) {
                configureBreakpointsForMethod(threadId, balStackFrames.get(1));
            }
        } catch (JdiProxyException e) {
            LOGGER.error(e.getMessage());
            int stepType = ((StepRequest) this.stepEventRequests.get(0)).depth();
            sendStepRequest(threadId, stepType);
        }
    }

    /**
     * Configures temporary(dynamic) breakpoints for all the lines within the method, which encloses the given stack
     * frame location. This strategy is used when processing STEP_OVER requests.
     */
    private void configureBreakpointsForMethod(long threadId, StackFrame frame) {
        try {
            Location currentLocation = frame.location();
            ReferenceType referenceType = currentLocation.declaringType();
            List<Location> allLocations = currentLocation.method().allLineLocations();
            Optional<Location> firstLocation = allLocations.stream().min(Comparator.comparingInt(Location::lineNumber));
            Optional<Location> lastLocation = allLocations.stream().max(Comparator.comparingInt(Location::lineNumber));
            if (firstLocation.isEmpty()) {
                return;
            }

            int nextStepPoint = firstLocation.get().lineNumber();
            do {
                List<Location> locations = referenceType.locationsOfLine(nextStepPoint);
                if (!locations.isEmpty() && (locations.get(0).lineNumber() > firstLocation.get().lineNumber())) {
                    BreakpointRequest bpReq = context.getEventManager().createBreakpointRequest(locations.get(0));
                    bpReq.enable();
                }
                nextStepPoint++;
            } while (nextStepPoint <= lastLocation.get().lineNumber());
        } catch (AbsentInformationException e) {
            LOGGER.error(e.getMessage());
            int stepType = ((StepRequest) this.stepEventRequests.get(0)).depth();
            sendStepRequest(threadId, stepType);
        }
    }

    private void createStepRequest(long threadId, int stepType) {
        context.getEventManager().deleteEventRequests(stepEventRequests);
        ThreadReferenceProxyImpl proxy = context.getAdapter().getAllThreads().get(threadId);
        if (proxy == null || proxy.getThreadReference() == null) {
            return;
        }

        StepRequest request = context.getEventManager().createStepRequest(proxy.getThreadReference(),
                StepRequest.STEP_LINE, stepType);
        request.setSuspendPolicy(StepRequest.SUSPEND_ALL);
        // Todo - Replace with a class inclusive filter.
        request.addClassExclusionFilter("io.*");
        request.addClassExclusionFilter("com.*");
        request.addClassExclusionFilter("org.*");
        request.addClassExclusionFilter("java.*");
        request.addClassExclusionFilter("$lambda$main$");
        stepEventRequests.add(request);
        request.addCountFilter(1);
        stepEventRequests.add(request);
        request.enable();
    }

    /**
     * Evaluates the given breakpoint condition (expression) using the ballerina debugger expression evaluation engine.
     *
     * @param expression      breakpoint expression
     * @param threadReference suspended thread reference, which should be used to get the top stack frame
     * @return result of the given breakpoint condition (logical expression).
     */
    private boolean evaluateBreakpointCondition(String expression, ThreadReference threadReference) {
        try {
            StackFrameProxyImpl frame = context.getAdapter().getAllThreads().get(threadReference.uniqueID()).frame(0);
            SuspendedContext ctx = new SuspendedContext(context.getSourceProject(), context.getDebuggeeVM(),
                    context.getAdapter().getAllThreads().get(threadReference.uniqueID()), frame);
            ExpressionEvaluator evaluator = new ExpressionEvaluator(ctx);
            String condition = evaluator.evaluate(expression).toString();
            return condition.equalsIgnoreCase(CONDITION_TRUE);
        } catch (JdiProxyException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates whether the given location is related to a ballerina source.
     *
     * @param location location
     * @return true if the given step event is related to a ballerina source
     */
    private boolean isBallerinaSource(Location location) {
        try {
            String sourceName = location.sourceName();
            int sourceLine = location.lineNumber();
            return sourceName.endsWith(BAL_FILE_EXT) && sourceLine > 0;
        } catch (AbsentInformationException e) {
            return false;
        }
    }

    /**
     * Checks whether the given source location lies within an external module source (i.e. lang library, standard
     * library, connector module).
     *
     * @param location source location
     */
    private static boolean isExternalLibSource(Location location) {
        try {
            String srcPath = location.sourcePath();
            return srcPath.startsWith(BALLERINA_ORG_PREFIX) || srcPath.startsWith(BALLERINAX_ORG_PREFIX);
        } catch (AbsentInformationException e) {
            return false;
        }
    }

    /**
     * Notifies DAP client that the remote VM is stopped due to a breakpoint hit / step event.
     */
    private void notifyStopEvent(Event event) {
        context.getEventManager().deleteEventRequests(stepEventRequests);
        StoppedEventArguments stoppedEventArguments = new StoppedEventArguments();

        if (event instanceof BreakpointEvent) {
            stoppedEventArguments.setReason(StoppedEventArgumentsReason.BREAKPOINT);
            stoppedEventArguments.setThreadId(((BreakpointEvent) event).thread().uniqueID());
        } else if (event instanceof StepEvent) {
            stoppedEventArguments.setReason(StoppedEventArgumentsReason.STEP);
            stoppedEventArguments.setThreadId(((StepEvent) event).thread().uniqueID());
        }

        stoppedEventArguments.setAllThreadsStopped(true);
        context.getClient().stopped(stoppedEventArguments);
    }
}
