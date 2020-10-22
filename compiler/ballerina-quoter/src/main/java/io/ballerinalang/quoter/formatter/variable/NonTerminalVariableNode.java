/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.quoter.formatter.variable;

import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerinalang.quoter.segment.generators.SegmentGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Non terminal segment node in the tree.
 */
public class NonTerminalVariableNode extends VariableNode implements Iterable<Segment> {
    private static Map<String, Integer> variableCount;
    private final NodeFactorySegment generated;
    private final NodeFactorySegment original;
    private int variableIndex;

    public NonTerminalVariableNode(NodeFactorySegment original, NonTerminalVariableNode parent) {
        super(parent);
        Objects.requireNonNull(variableCount);
        this.variableIndex = -1;
        this.generated = SegmentGenerator.createFactoryCallSegment(original.getMethodName());
        this.original = original;
    }

    /**
     * Reset the variable cache.
     * Must be called before using the objects.
     */
    public static void resetVariableCount() {
        variableCount = new HashMap<>();
    }

    /**
     * Add a segment as a parameter.
     */
    public void addNewSegmentParameter(Segment segment) {
        generated.addParameter(segment);
    }

    /**
     * Get the return type of the function call.
     * Uses the method name.
     */
    private String getType() {
        String rawMethodName = original.getMethodName().substring(6);
        if (rawMethodName.contains("MinutiaeList")) {
            return "MinutiaeList";
        } else if (rawMethodName.contains("Minutiae")) {
            return "Minutiae";
        }
        return original.getMethodName().substring(6);
    }

    @Override
    public void markAsVisited() {
        String type = getType();
        if (variableCount.containsKey(type)) {
            variableCount.put(type, variableCount.get(type) + 1);
        } else {
            variableCount.put(type, 0);
        }
        variableIndex = variableCount.get(type);
        super.markAsVisited();
    }

    @Override
    protected String getParameterRepresentation() {
        String varType = getType();
        String countStr = (variableIndex == 0 ? "" : String.valueOf(variableIndex));
        String nameStr = varType.substring(0, 1).toLowerCase() + varType.substring(1);
        return nameStr + countStr;
    }

    @Override
    public String toString() {
        String spacedDeclaration = generated.toString().replace(",", ", ");
        // TODO: Fix the return type of createSeparatedNodeList
        if (getType().equals("SeparatedNodeList")) {
            spacedDeclaration = "(SeparatedNodeList) " + spacedDeclaration;
        }
        return String.format("%s %s = %s;", getType(), getParameterRepresentation(), spacedDeclaration);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Segment> iterator() {
        return original.iterator();
    }
}
