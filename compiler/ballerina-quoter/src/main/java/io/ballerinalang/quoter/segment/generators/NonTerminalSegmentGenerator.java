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
package io.ballerinalang.quoter.segment.generators;

import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerinalang.quoter.segment.generators.cache.MethodCache;
import io.ballerinalang.quoter.segment.generators.cache.MethodReference;
import io.ballerinalang.quoter.segment.generators.cache.ParameterNameCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts non terminal into a segment.
 */
public class NonTerminalSegmentGenerator {
    private final ParameterNameCache parameterNameCache;
    private final MethodCache methodCache;
    private final NodeSegmentGenerator nodeSegmentGenerator;

    public NonTerminalSegmentGenerator(NodeSegmentGenerator nodeSegmentGenerator,
                                       ParameterNameCache parameterNameCache,
                                       MethodCache methodCache) {
        this.nodeSegmentGenerator = nodeSegmentGenerator;
        this.parameterNameCache = parameterNameCache;
        this.methodCache = methodCache;
    }

    /**
     * Converts every NonTerminalNode to a Segment.
     * Uses reflection to find the required factory method call in runtime.
     */
    public Segment createSegment(NonTerminalNode node) {
        MethodReference method = getNonTerminalNodeProcessMethod(node);
        NodeFactorySegment root = method.toSegment();

        // Get all the possible child names for the current node type
        List<String> parameterName = parameterNameCache.getParameterNames(node.getClass().getSimpleName());

        if (method.requiresSyntaxKind()) {
            root.addParameter(SegmentGenerator.createSyntaxKindSegment(node.kind()));
        }

        List<ChildNodeEntry> childNodeEntries = new ArrayList<>(node.childEntries());
        int childNodeEntriesIndex = 0; // Current processing childNodeEntry
        for (int i = 0; i < parameterName.size(); i++) {
            String childName = parameterName.get(i);

            if (childNodeEntriesIndex < childNodeEntries.size()) {
                ChildNodeEntry childNodeEntry = childNodeEntries.get(childNodeEntriesIndex);
                if (childNodeEntry.name().equals(childName)) {
                    childNodeEntriesIndex++;
                    root.addParameter(createNodeOrNodeList(childNodeEntry, method, i));
                    continue;
                }
            }
            // Not processed
            root.addParameter(SegmentGenerator.createNullSegment());
        }

        return root;
    }


    /**
     * Create a Node or a NodeList from a given ChildNodeEntry.
     * Since NodeList can be either SeparatedNodeList or NodeList, type required by the method is needed.
     * So the method and the param index is also passed.
     */
    private Segment createNodeOrNodeList(ChildNodeEntry nodeEntry, MethodReference method, int paramIndex) {
        if (nodeEntry.isList()) {
            return createNodeListSegment(nodeEntry.nodeList(), method, paramIndex);
        } else if (nodeEntry.node().isPresent()) {
            Node childNode = nodeEntry.node().get();
            return nodeSegmentGenerator.createNodeSegment(childNode);
        } else {
            return SegmentGenerator.createNullSegment();
        }
    }

    /**
     * Creates a Node List segment if the node is actually a NodeList.
     * Parameter type which is required by the parent function call determines whether to
     * create a SeparatedNodeList or a NodeList.
     * This is found via method reference and param index.
     */
    private Segment createNodeListSegment(NodeList<Node> nodes, MethodReference method, int paramIndex) {
        // Create a Segment array list from the child nodes.
        ArrayList<Segment> segments = new ArrayList<>();
        nodes.forEach(node -> segments.add(nodeSegmentGenerator.createNodeSegment(node)));

        String genericType = method.getParameterGeneric(paramIndex);
        // Get if the call requires a NodeList or a SeparatedNodeList.
        // TODO This is a fix for the issue of SeparatedNodeList having a different return type (#26378)
        NodeFactorySegment root = (method.getParameterType(paramIndex) == SeparatedNodeList.class)
                ? SegmentGenerator.createSeparatedNodeListSegment(genericType)
                : SegmentGenerator.createFactoryCallSegment("createNodeList", genericType);
        segments.forEach(root::addParameter);
        return root;
    }

    /**
     * Retrieves the method to process the said node.
     * If the node is a IdentifierToken then it searches for createIdentifierToken.
     */
    private MethodReference getNonTerminalNodeProcessMethod(NonTerminalNode node) {
        String methodName = "create" + node.getClass().getSimpleName();
        return methodCache.getMethod(methodName);
    }
}
