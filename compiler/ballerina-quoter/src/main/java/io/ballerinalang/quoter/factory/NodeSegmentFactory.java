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
package io.ballerinalang.quoter.factory;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Handles Node to Segment conversion.
 */
public class NodeSegmentFactory {

    /**
     * Coverts a Node to a Segment(NonTerminal or a Terminal).
     * Does not handle Minutiae Nodes. Should be a NonTerminal or a Token.
     * If the node is null, returns null segment.
     * If the node type is something other, throws a Runtime Exception.
     */
    public static Segment createNodeSegment(Node node) {
        if (node == null) {
            return SegmentFactory.createNullSegment();
        } else if (node instanceof Token) {
            return TokenSegmentFactory.createTokenSegment((Token) node);
        } else if (node instanceof NonTerminalNode) {
            return createNonTerminalNodeSegment((NonTerminalNode) node);
        } else {
            throw new RuntimeException("Unexpected node type for: " + node);
        }
    }

    /**
     * Converts every NonTerminalNode to a Segment.
     * Uses reflection to find the required factory method call in runtime.
     */
    private static Segment createNonTerminalNodeSegment(NonTerminalNode node) {
        Method method = getNonTerminalNodeProcessMethod(node);
        Type[] parameterTypes = method.getParameterTypes();
        NodeFactorySegment root = SegmentFactory.createNodeFactoryMethodSegment(method.getName());

        // If first param is of SyntaxKind, add relevant node and shift array list by 1
        if (parameterTypes.length > 0 && parameterTypes[0] == SyntaxKind.class) {
            root.addParameter(SegmentFactory.createSyntaxKindSegment(node.kind()));
            System.arraycopy(parameterTypes, 1, parameterTypes, 0, parameterTypes.length - 1);
        }

        // Get all the possible child names for the current node type
        // TODO: Preferably following line should be List<String> childNames = node.getAllChildNames(); or similar
        List<String> childNames = ChildEntryNameSingleton.getInstance().childEntryNames(node.getClass().getSimpleName());

        List<ChildNodeEntry> childNodeEntries = new ArrayList<>(node.childEntries());
        int childNodeEntriesIndex = 0; // Current processing childNodeEntry
        for (int i = 0; i < childNames.size(); i++) {
            String childName = childNames.get(i);

            // Process only if the current index is a valid index.
            if (childNodeEntriesIndex < childNodeEntries.size()) {
                ChildNodeEntry childNodeEntry = childNodeEntries.get(childNodeEntriesIndex);

                // If the childNodeEntry(from the node) name and the childName(From method) is the same.
                // They are not equal if a entry is missing from the tree structure but
                // the method requires that parameter.
                if (childNodeEntry.name().equals(childName)) {
                    childNodeEntriesIndex++;
                    root.addParameter(createNodeOrNodeList(childNodeEntry, parameterTypes[i]));
                    continue;
                }
            }
            // Not processed
            root.addParameter(SegmentFactory.createNullSegment());
        }

        return root;
    }

    /**
     * Create a Node or a NodeList from a given ChildNodeEntry.
     * Since NodeList can be either SeparatedNodeList or NodeList, type required by the method is taken.
     */
    private static Segment createNodeOrNodeList(ChildNodeEntry nodeEntry, Type requiredType) {
        if (nodeEntry.isList()) {
            return createNodeListSegment(nodeEntry.nodeList(), requiredType);
        } else if (nodeEntry.node().isPresent()) {
            Node childNode = nodeEntry.node().get();
            return createNodeSegment(childNode);
        } else {
            // If node entry is neither a NodeList nor present
            return SegmentFactory.createNullSegment();
        }
    }

    /**
     * Creates a Node List segment if the node is actually a NodeList.
     * Parameter type which is required by the parent function call determines whether to
     * create a SeparatedNodeList or a NodeList.
     */
    private static Segment createNodeListSegment(NodeList<Node> nodes, Type parameterType) {
        // Create a Segment array list from the child nodes.
        ArrayList<Segment> segments = new ArrayList<>();
        nodes.forEach(node -> segments.add(NodeSegmentFactory.createNodeSegment(node)));

        // Get if the call requires a NodeList or a SeparatedNodeList.
        if (parameterType == SeparatedNodeList.class) {
            // Create a SeparatedNodeList constructor factory call. Only add the even elements. (Odd ones are commas)
            NodeFactorySegment root = SegmentFactory.createNodeFactoryMethodSegment("createSeparatedNodeList");
            for (int i = 0; i < segments.size(); i += 2) {
                root.addParameter(segments.get(i));
            }
            // TODO This is a fix for the issue of SeparatedNodeList having a different return type (#26378)
            return SegmentFactory.createCodeSegment("(SeparatedNodeList)" + root.toString());
        } else {
            // Create a NodeList factory call.
            NodeFactorySegment root = SegmentFactory.createNodeFactoryMethodSegment("createNodeList");
            segments.forEach(root::addParameter);
            return root;
        }
    }

    /**
     * Retrieved the method to process the said node.
     * This uses reflection to find the method.
     * If the node is a IdentifierToken then it searches for createIdentifierToken.
     * If the method is not found throws a RuntimeException.
     */
    private static Method getNonTerminalNodeProcessMethod(NonTerminalNode node) {
        String methodName = "create" + node.getClass().getSimpleName();
        Method[] methodNames = NodeFactory.class.getMethods();
        for (Method method : methodNames) {
            if (method.getName().equals(methodName)) return method;
        }
        throw new RuntimeException("Unexpected node type [" + node.getClass().getSimpleName() + "] for: " + node);
    }
}
