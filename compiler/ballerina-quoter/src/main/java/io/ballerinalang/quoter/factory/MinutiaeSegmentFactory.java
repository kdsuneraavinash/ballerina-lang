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

import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.Segment;

/**
 * Handles Minutiae to Segment conversion.
 */
public class MinutiaeSegmentFactory {
    /**
     * Converts a MinutiaeList to a Segment.
     * Used as `MinutiaeSegmentGenerator.createMinutiaeList(token.leadingMinutiae())`
     */
    public static Segment createMinutiaeListSegment(MinutiaeList minutiaeList) {
        if (minutiaeList.isEmpty()) return SegmentFactory.createNodeFactoryMethodSegment("createEmptyMinutiaeList");

        // If the list is not empty, create the factory segment and add every minutiae segment
        NodeFactorySegment minutiaeListMethod = SegmentFactory.createNodeFactoryMethodSegment("createMinutiaeList");
        minutiaeList.forEach(minutiae -> minutiaeListMethod.addParameter(createMinutiaeSegment(minutiae)));
        return minutiaeListMethod;
    }

    /**
     * Converts a Minutiae to a Segment.
     * Used by `createMinutiaeListSegment` to convert all minutiae elements to segments.
     */
    private static Segment createMinutiaeSegment(Minutiae minutiae) {
        // Decide on method to use
        String factoryMethod;
        if (minutiae.kind() == SyntaxKind.COMMENT_MINUTIAE) {
            factoryMethod = "createCommentMinutiae";
        } else if (minutiae.kind() == SyntaxKind.WHITESPACE_MINUTIAE) {
            factoryMethod = "createWhitespaceMinutiae";
        } else if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
            factoryMethod = "createEndOfLineMinutiae";
        } else if (minutiae.kind() == SyntaxKind.INVALID_NODE_MINUTIAE) {
            throw new RuntimeException("Invalid node minutiae found with text: " + minutiae.text());
        } else {
            throw new RuntimeException("Unexpected Minutiae found");
        }

        // All minutiae factory methods accept only the text
        NodeFactorySegment nodeFactorySegment = SegmentFactory.createNodeFactoryMethodSegment(factoryMethod);
        nodeFactorySegment.addParameter(SegmentFactory.createStringSegment(minutiae.text()));
        return nodeFactorySegment;
    }
}
