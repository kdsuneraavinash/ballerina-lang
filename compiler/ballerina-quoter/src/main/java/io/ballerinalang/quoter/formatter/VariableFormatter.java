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

import io.ballerinalang.quoter.formatter.variable.NonTerminalVariableNode;
import io.ballerinalang.quoter.formatter.variable.VariableNode;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;

import java.util.*;

/**
 * Formats the code so that each line will have one method call.
 */
public class VariableFormatter extends SegmentFormatter {
    final Map<String, Integer> variableCount;

    public VariableFormatter() {
        this.variableCount = new HashMap<>();
    }

    @Override
    public String format(Segment segment) {
        // Must reset the variable counts
        NonTerminalVariableNode.resetVariableCount();
        ArrayList<String> lines = new ArrayList<>();
        Queue<VariableNode> queue = new ArrayDeque<>();

        if (!(segment instanceof NodeFactorySegment)) return segment.toString();
        queue.add(new NonTerminalVariableNode((NodeFactorySegment) segment, null));

        // Add each child 2 times and on the second time it is added to the node list.
        // This is to ensure that a node will be added only after all the nodes that
        // should be defined first is added.
        // This algorithm is a slight variation of BFS.
        //
        // Eg Order of processing for A(B(C())) tree: A -> B -> A[x] -> C -> B[x] -> C[X]
        // So, A, B, C are defined in this order: c = C(), b = B(c), a = A(b)
        while (!queue.isEmpty()) {
            VariableNode current = queue.remove();

            if (current.isVisited() && current instanceof NonTerminalVariableNode) {
                lines.add(current.toString());
            } else if (current instanceof NonTerminalVariableNode) {
                NonTerminalVariableNode nonTerminalCurrent = (NonTerminalVariableNode) current;
                for (Segment child : nonTerminalCurrent) {
                    VariableNode variableNode = VariableNode.create(child, nonTerminalCurrent);
                    queue.add(variableNode);
                }
                nonTerminalCurrent.markAsVisited();
                queue.add(current);
            } else if (!current.isVisited()) {
                current.markAsVisited();
                queue.add(current);
            }
        }

        // Convert to string representation
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = lines.size() - 1; i > -1; i--) {
            String line = lines.get(i);
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }
}