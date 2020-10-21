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

/**
 * Handles Tokens(Leaf Nodes) to Segment conversion.
 */
public class TokenSegmentFactory {
    /**
     * Converts Token to Segment.
     * Handles minutia of the token as well.
     */
    public static NodeFactorySegment createTokenSegment(Token token) {
        // Decide on the method and add all parameters required, except for minutiae parameters.
        // If there are no minutiae and the token constructor supports calling without minutiae, use that call.
        NodeFactorySegment root;
        boolean noMinutiae = token.leadingMinutiae().isEmpty() && token.trailingMinutiae().isEmpty();

        // Decide on factory call and add parameters(except minutiae)
        if (token instanceof LiteralValueToken) {
            root = SegmentFactory.createNodeFactoryMethodSegment("createLiteralValueToken");
            root.addParameter(SegmentFactory.createSyntaxKindSegment(token.kind()));
            root.addParameter(SegmentFactory.createStringSegment(token.text()));
        } else if (token instanceof IdentifierToken) {
            root = SegmentFactory.createNodeFactoryMethodSegment("createIdentifierToken");
            root.addParameter(SegmentFactory.createStringSegment(token.text()));
            if (noMinutiae) return root;
        } else if (token instanceof DocumentationLineToken) {
            root = SegmentFactory.createNodeFactoryMethodSegment("createDocumentationLineToken");
            root.addParameter(SegmentFactory.createStringSegment(token.text()));
        } else {
            root = SegmentFactory.createNodeFactoryMethodSegment("createToken");
            root.addParameter(SegmentFactory.createSyntaxKindSegment(token.kind()));
            if (noMinutiae) return root;
        }

        // Add leading and trailing minutiae parameters to the call.
        root.addParameter(MinutiaeSegmentFactory.createMinutiaeListSegment(token.leadingMinutiae()));
        root.addParameter(MinutiaeSegmentFactory.createMinutiaeListSegment(token.trailingMinutiae()));
        return root;
    }
}
