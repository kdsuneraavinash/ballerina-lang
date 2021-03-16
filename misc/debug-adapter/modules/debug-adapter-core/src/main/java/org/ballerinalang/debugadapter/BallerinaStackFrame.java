/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.jdi.StackFrameProxyImpl;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.StackFrame;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinalang.debugadapter.JBallerinaDebugServer.isBalStackFrame;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getRectifiedSourcePath;
import static org.ballerinalang.debugadapter.variable.VariableUtils.removeRedundantQuotes;
import static org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper.LAMBDA;

/**
 * Represents a ballerina stack frame instance.
 *
 * @since 2.0.0
 */
public class BallerinaStackFrame {

    private final ExecutionContext context;
    private final Long frameId;
    private final StackFrameProxyImpl jStackFrame;
    private StackFrame dapStackFrame;

    private static final String STRAND_FIELD_NAME = "name";
    private static final String FRAME_TYPE_START = "start";
    private static final String FRAME_TYPE_WORKER = "worker";
    private static final String FRAME_TYPE_ANONYMOUS = "anonymous";
    private static final String FRAME_SEPARATOR = ":";
    private static final String WORKER_LAMBDA_REGEX = "(\\$lambda\\$)\\b(.*)\\b(\\$lambda)(.*)";

    public BallerinaStackFrame(ExecutionContext context, Long frameId, StackFrameProxyImpl stackFrameProxy) {
        this.context = context;
        this.frameId = frameId;
        this.jStackFrame = stackFrameProxy;
    }

    /**
     * Returns a debugger adapter protocol compatible instance of this breakpoint.
     *
     * @return as an instance of {@link org.eclipse.lsp4j.debug.StackFrame}
     */
    public Optional<StackFrame> getAsDAPStackFrame() {
        dapStackFrame = Objects.requireNonNullElse(dapStackFrame, computeDapStackFrame());
        return Optional.ofNullable(dapStackFrame);
    }

    private StackFrame computeDapStackFrame() {
        try {
            if (!isBalStackFrame(jStackFrame.getStackFrame())) {
                return null;
            }

            StackFrame dapStackFrame = new StackFrame();
            dapStackFrame.setId(frameId);
            dapStackFrame.setName(getStackFrameName(jStackFrame));
            dapStackFrame.setLine((long) jStackFrame.location().lineNumber());
            dapStackFrame.setColumn(0L);

            // Adds ballerina source information.
            Path sourcePath = getRectifiedSourcePath(jStackFrame.location(), context.getSourceProject());
            if (sourcePath != null) {
                Source source = new Source();
                source.setPath(sourcePath.toString());
                source.setName(jStackFrame.location().sourceName());
                dapStackFrame.setSource(source);
            }
            return dapStackFrame;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Derives ballerina stack frame name from the given java stack frame instance.
     *
     * @param stackFrame JDI stack frame instance
     * @return Ballerina stack frame name
     */
    private static String getStackFrameName(StackFrameProxyImpl stackFrame) {
        try {
            String frameName;
            ObjectReference strand = getStrand(stackFrame);
            if (strand != null) {
                Value frameNameValue = strand.getValue(strand.referenceType().fieldByName(STRAND_FIELD_NAME));
                if (frameNameValue != null) {
                    frameName = removeRedundantQuotes(String.valueOf(frameNameValue));
                } else {
                    frameName = FRAME_TYPE_ANONYMOUS;
                }
            } else {
                frameName = FRAME_TYPE_ANONYMOUS;
            }

            if (stackFrame.location().method().name().matches(WORKER_LAMBDA_REGEX)) {
                return FRAME_TYPE_WORKER + FRAME_SEPARATOR + frameName;
            } else if (stackFrame.location().method().name().contains(LAMBDA)) {
                return stackFrame.visibleVariableByName(STRAND_VAR_NAME) != null ? frameName :
                        FRAME_TYPE_START + FRAME_SEPARATOR + frameName;
            } else {
                return stackFrame.location().method().name();
            }
        } catch (Exception e) {
            return FRAME_TYPE_ANONYMOUS;
        }
    }

    /**
     * Retrieves ballerina strand instance of the given stack frame.
     */
    private static ObjectReference getStrand(StackFrameProxyImpl frame) {
        try {
            if (frame.visibleVariableByName(STRAND_VAR_NAME) == null) {
                return (ObjectReference) ((ArrayReference) frame.getStackFrame().getArgumentValues().get(0))
                        .getValue(0);
            }
            return (ObjectReference) frame.getValue(frame.visibleVariableByName(STRAND_VAR_NAME));
        } catch (Exception e) {
            return null;
        }
    }
}
