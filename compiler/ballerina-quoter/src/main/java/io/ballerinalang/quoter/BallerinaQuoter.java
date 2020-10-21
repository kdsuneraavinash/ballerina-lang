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
import io.ballerinalang.quoter.formatter.SegmentFormatter;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerinalang.quoter.segment.generators.NodeSegmentGenerator;

import java.io.*;
import java.util.Scanner;

import static io.ballerinalang.quoter.QuoterConfig.*;

public class BallerinaQuoter {
    public static void main(String[] args) {
        try {
            // 1) Load quoter properties
            QuoterConfig quoterConfig = QuoterConfig.getInstance();
            // 2) Get the input file code
            String sourceCode = readInputFile(quoterConfig);
            // 3) Create the factory
            NodeSegmentGenerator factory = NodeSegmentGenerator.fromConfig(quoterConfig);
            // 4) Get the formatter
            SegmentFormatter formatter = SegmentFormatter.getFormatter(quoterConfig);

            // 5) Execute the generator
            String generatedCode = execute(sourceCode, factory, formatter);

            // 6) Output the generated code
            outputString(quoterConfig, generatedCode);

        } catch (QuoterException exception) {
            System.out.println("There was an Exception when parsing. Please check your code.\nError: " + exception);
        }
    }

    /**
     * Execute the generator.
     * sourceCode -> [nodeSegmentFactory] -> segment -> [formatter] -> generatedCode
     */
    private static String execute(String sourceCode, NodeSegmentGenerator nodeSegmentGenerator, SegmentFormatter formatter) {
        // Create syntax tree
        TextDocument sourceCodeDocument = TextDocuments.from(sourceCode);
        Node syntaxTreeNode = SyntaxTree.from(sourceCodeDocument).rootNode();
        // Convert tree to segment
        Segment segment = nodeSegmentGenerator.createNodeSegment(syntaxTreeNode);
        // Format using the formatter
        return formatter.format(segment);
    }

    /**
     * Read input from the file specified in the configurations.
     */
    private static String readInputFile(QuoterConfig config) {
        String inputFileName = config.getOrThrow(EXTERNAL_INPUT_FILE);

        try (InputStream inputStream = new FileInputStream(inputFileName)) {
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            throw new QuoterException("Failed to read " + inputFileName + ". Error: " + e.getMessage(), e);
        }
    }

    /**
     * Output the final output in the way specified in the configurations.
     */
    private static void outputString(QuoterConfig config, String content) {
        if (config.getBooleanOrThrow(EXTERNAL_OUTPUT_SYS_OUT)) {
            System.out.println(content);
            return;
        }

        String outputFileName = config.getOrThrow(EXTERNAL_OUTPUT_FILE);
        try (OutputStream outputStream = new FileOutputStream(outputFileName)) {
            outputStream.write(content.getBytes());
        } catch (IOException e) {
            throw new QuoterException("Failed to write " + outputFileName + ". Error: " + e.getMessage(), e);
        }
    }
}
