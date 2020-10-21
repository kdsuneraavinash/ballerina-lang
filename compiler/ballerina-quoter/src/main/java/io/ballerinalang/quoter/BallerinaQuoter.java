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

package io.ballerinalang.quoter;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerinalang.quoter.formatter.NewLineParenFormatter;
import io.ballerinalang.quoter.formatter.SegmentFormatter;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerinalang.quoter.factory.NodeSegmentFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class BallerinaQuoter {
    public static void main(String[] args) {
        try {
            SegmentFormatter formatter = new NewLineParenFormatter();
            String code = "var x= 4;";
            TextDocument sourceCodeDocument = TextDocuments.from(code);
            Node syntaxTreeNode = SyntaxTree.from(sourceCodeDocument).rootNode();
            Segment segment = NodeSegmentFactory.createNodeSegment(syntaxTreeNode);
            String generatedCode = formatter.format(segment);
            System.out.println(generatedCode);
        } catch (Exception exception) {
            System.out.println("There was an Exception when parsing. Please check your code.\nError: " + exception);
        }
    }
}
