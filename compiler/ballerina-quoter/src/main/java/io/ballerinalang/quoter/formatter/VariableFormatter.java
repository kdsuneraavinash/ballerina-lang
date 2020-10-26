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
package io.ballerinalang.quoter.formatter;

import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerinalang.quoter.segment.generators.SegmentGenerator;

import java.util.HashMap;
import java.util.Stack;

/**
 * Grouped Variable formatter.
 * Groups minutiae with corresponding node.
 */
public class VariableFormatter extends SegmentFormatter {
    private HashMap<String, Integer> variableCount;
    private final SegmentFormatter formatter;

    /**
     * Data structure to hold string with an variable name.
     */
    private class NamedContent {
        final String name;
        String content;

        NamedContent(String type) {
            // Find the variable name: var, var1, var2, ...
            String varGenericName = type.substring(0, 1).toLowerCase() + type.substring(1);
            int varGenericCount = variableCount.getOrDefault(varGenericName, 0);
            this.name = varGenericName + (varGenericCount == 0 ? "" : String.valueOf(varGenericCount));
            variableCount.put(varGenericName, varGenericCount + 1);
        }

        void setContent(String string) {
            this.content = string;
        }

        @Override
        public String toString() {
            return content;
        }
    }

    VariableFormatter() {
        formatter = new NoFormatter();
    }

    @Override
    public String format(Segment segment) {
        variableCount = new HashMap<>();
        return "MinutiaeList trailingMinutiae, leadingMinutiae;\n\n" +
                processNode((NodeFactorySegment) segment);
    }

    /**
     * Processes a token and returns variable name and the content that should come before.
     */
    private NamedContent processToken(NodeFactorySegment token) {
        StringBuilder stringBuilder = new StringBuilder();
        Stack<Segment> params = new Stack<>();
        token.forEach(params::push);

        NamedContent namedContent = new NamedContent(token.getType());
        NodeFactorySegment factorySegment = SegmentGenerator.createFactoryCallSegment(token.getMethodName());

        // Define minutiae
        stringBuilder.append("trailingMinutiae = ").append(formatter.format(params.pop())).append(";\n");
        stringBuilder.append("leadingMinutiae = ").append(formatter.format(params.pop())).append(";\n");

        // Add params and minutiae
        while (!params.isEmpty()) {
            factorySegment.addParameter(params.remove(0));
        }
        factorySegment.addParameter(SegmentGenerator.createCodeSegment("leadingMinutiae"));
        factorySegment.addParameter(SegmentGenerator.createCodeSegment("trailingMinutiae"));

        stringBuilder.append(token.getType()).append(" ").append(namedContent.name)
                .append(" = ").append(factorySegment).append(";\n\n");

        namedContent.setContent(stringBuilder.toString());
        return namedContent;
    }

    /**
     * Processes node and returns variable name and the content that should come before.
     */
    private NamedContent processNode(NodeFactorySegment segment) {
        // If it is a token, handle accordingly
        if (segment.getMethodName().endsWith("Token")) {
            return processToken(segment);
        }

        // Get each child and add the content that should come before
        StringBuilder stringBuilder = new StringBuilder();
        NodeFactorySegment factorySegment = SegmentGenerator.createFactoryCallSegment(segment.getMethodName(), segment.getGenericType());
        for (Segment child : segment) {
            if (child instanceof NodeFactorySegment) {
                NodeFactorySegment childFactoryCall = (NodeFactorySegment) child;
                NamedContent namedContent = processNode(childFactoryCall);
                stringBuilder.append(namedContent.content);
                factorySegment.addParameter(SegmentGenerator.createCodeSegment(namedContent.name));
            } else {
                factorySegment.addParameter(child);
            }
        }

        // Node definition
        NamedContent namedContent = new NamedContent(segment.getType());
        stringBuilder.append(factorySegment.getType());
        if (factorySegment.getGenericType() != null) {
            stringBuilder.append("<").append(factorySegment.getGenericType()).append(">");
        }
        stringBuilder.append(" ").append(namedContent.name)
                .append(" = ").append(factorySegment).append(";\n\n");
        namedContent.setContent(stringBuilder.toString());
        return namedContent;
    }
}

