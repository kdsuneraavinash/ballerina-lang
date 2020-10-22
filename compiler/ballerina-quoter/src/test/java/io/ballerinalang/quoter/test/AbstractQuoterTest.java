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
package io.ballerinalang.quoter.test;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerinalang.quoter.config.QuoterConfig;
import io.ballerinalang.quoter.QuoterException;
import io.ballerinalang.quoter.formatter.SegmentFormatter;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerinalang.quoter.segment.generators.NodeSegmentGenerator;
import net.openhft.compiler.CachedCompiler;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

public class AbstractQuoterTest {
    protected static String readResource(String path) {
        ClassLoader classLoader = AbstractQuoterTest.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) throw new QuoterException("File not found: " + path);
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            throw new QuoterException("Failed to read " + path + ". Error: " + e.getMessage(), e);
        }
    }

    protected Segment getSegmentFromFile(String fileName, QuoterConfig config) {
        String sourceCode = readResource(fileName);
        NodeSegmentGenerator generator = NodeSegmentGenerator.fromConfig(config);

        TextDocument sourceCodeDocument = TextDocuments.from(sourceCode);
        Node syntaxTreeNode = SyntaxTree.from(sourceCodeDocument).rootNode();
        return generator.createNodeSegment(syntaxTreeNode);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected SyntaxTree createSegmentAndRun(String balFile) {
        try {
            String templateName = "dynamic-loading.java";
            String formatterName = "template";
            int tabSpace = 2;
            QuoterConfig config = new TestQuoterConfig(templateName, tabSpace, formatterName);
            Segment segment = getSegmentFromFile(balFile, config);
            String javaCode = SegmentFormatter.getFormatter(config).format(segment);

            ClassLoader classLoader = new ClassLoader() {
            };
            CachedCompiler compiler = new CachedCompiler(null, null);
            Objects.requireNonNull(compiler);

            String className = "templatepkg.TemplateCodeImpl";
            Class templateCodeImpl = compiler.loadFromJava(classLoader, className, javaCode);
            TemplateCode templateCode = (TemplateCode) templateCodeImpl.getDeclaredConstructor().newInstance();

            return templateCode.getNode().syntaxTree();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected void testForSameOutput(String fileName) {
        SyntaxTree tree = createSegmentAndRun(fileName);
        String targetCode = readResource(fileName);
        Assert.assertEquals(tree.toSourceCode().trim(), targetCode.trim());
    }

    protected void testAssertionFiles(String directory, String filePrefix) {
        testForSameOutput(directory + "/" + filePrefix + ".bal");
    }


}
