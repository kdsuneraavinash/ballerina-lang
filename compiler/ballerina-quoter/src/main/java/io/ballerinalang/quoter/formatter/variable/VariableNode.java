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

import io.ballerinalang.quoter.segment.CodeSegment;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;

/**
 * Node denoting a segment in segment tree for variable formatter.
 */
public abstract class VariableNode {
    private boolean visited;
    protected final NonTerminalVariableNode parent;

    protected VariableNode(NonTerminalVariableNode parent) {
        this.parent = parent;
        visited = false;
    }

    /**
     * Create suitable variable node.
     */
    public static VariableNode create(Segment segment, NonTerminalVariableNode parent) {
        if (segment instanceof NodeFactorySegment) {
            return new NonTerminalVariableNode((NodeFactorySegment) segment, parent);
        } else {
            return new TerminalVariableNode(segment, parent);
        }
    }

    /**
     * Whether this was not visited.
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * Mark this node as visited.
     */
    public void markAsVisited() {
        visited = true;
        addToParent();
    }

    /**
     * Adds the parameter to the parent
     */
    protected void addToParent() {
        if (parent != null) {
            Segment parameterSegment = new CodeSegment(getParameterRepresentation());
            parent.addNewSegmentParameter(parameterSegment);
        }
    }

    /**
     * How this should look like inside function call.
     */
    protected abstract String getParameterRepresentation();
}
