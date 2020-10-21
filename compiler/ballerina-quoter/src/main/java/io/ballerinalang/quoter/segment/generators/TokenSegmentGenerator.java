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

import io.ballerina.compiler.syntax.tree.*;
import io.ballerinalang.quoter.segment.NodeFactorySegment;

/**
 * Handles Tokens(Leaf Nodes) to Segment conversion.
 */
public class TokenSegmentGenerator {
    /**
     * Converts Token to Segment.
     * Handles minutia of the token as well.
     */
    static NodeFactorySegment createTokenSegment(Token token) {
        // Decide on the method and add all parameters required, except for minutiae parameters.
        // If there are no minutiae and the token constructor supports calling without minutiae, use that call.
        NodeFactorySegment root;
        boolean noMinutiae = token.leadingMinutiae().isEmpty() && token.trailingMinutiae().isEmpty();

        // Decide on factory call and add parameters(except minutiae)
        if (token instanceof LiteralValueToken) {
            root = SegmentGenerator.createFactoryCallSegment("createLiteralValueToken");
            root.addParameter(SegmentGenerator.createSyntaxKindSegment(token.kind()));
            root.addParameter(SegmentGenerator.createStringSegment(token.text()));
        } else if (token instanceof IdentifierToken) {
            root = SegmentGenerator.createFactoryCallSegment("createIdentifierToken");
            root.addParameter(SegmentGenerator.createStringSegment(token.text()));
            if (noMinutiae) return root;
        } else if (token instanceof DocumentationLineToken) {
            root = SegmentGenerator.createFactoryCallSegment("createDocumentationLineToken");
            root.addParameter(SegmentGenerator.createStringSegment(token.text()));
        } else {
            root = SegmentGenerator.createFactoryCallSegment("createToken");
            root.addParameter(SegmentGenerator.createSyntaxKindSegment(token.kind()));
            if (noMinutiae) return root;
        }

        // Add leading and trailing minutiae parameters to the call.
        root.addParameter(MinutiaeSegmentGenerator.createMinutiaeListSegment(token.leadingMinutiae()));
        root.addParameter(MinutiaeSegmentGenerator.createMinutiaeListSegment(token.trailingMinutiae()));
        return root;
    }
}
