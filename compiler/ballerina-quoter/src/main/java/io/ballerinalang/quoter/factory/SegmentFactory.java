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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.quoter.segment.CodeSegment;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.StringSegment;
import io.ballerinalang.quoter.segment.SyntaxKindSegment;

/**
 * Handles base segment creation through helper methods.
 */
public class SegmentFactory {
    /**
     * Creates a factory method with the given method name.
     */
    public static NodeFactorySegment createNodeFactoryMethodSegment(String methodName) {
        return new NodeFactorySegment(methodName);
    }

    /**
     * Create a basic code segment.
     */
    public static CodeSegment createCodeSegment(String code) {
        return new CodeSegment(code);
    }

    /**
     * Create a string literal segment.
     */
    public static StringSegment createStringSegment(String string) {
        return new StringSegment(string);
    }

    /**
     * Creates a SyntaxKind enum segment.
     */
    public static SyntaxKindSegment createSyntaxKindSegment(SyntaxKind syntaxKind) {
        return new SyntaxKindSegment(syntaxKind);
    }

    /**
     * Helper function to create a null code segment.
     */
    public static CodeSegment createNullSegment() {
        return createCodeSegment("null");
    }
}
