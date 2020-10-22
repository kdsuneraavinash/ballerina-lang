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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerinalang.quoter.QuoterException;
import io.ballerinalang.quoter.config.QuoterConfig;
import io.ballerinalang.quoter.segment.Segment;
import io.ballerinalang.quoter.segment.generators.cache.MethodCache;
import io.ballerinalang.quoter.segment.generators.cache.ParameterNameCache;

/**
 * Handles Node to Segment conversion.
 */
public class NodeSegmentGenerator {
    private final NonTerminalSegmentGenerator nonTerminalSegmentGenerator;

    private NodeSegmentGenerator(ParameterNameCache parameterNameCache, MethodCache methodCache) {
        this.nonTerminalSegmentGenerator =
                new NonTerminalSegmentGenerator(this, parameterNameCache, methodCache);
    }

    /**
     * Use the parameter name from the config to create a cache and the factory.
     */
    public static NodeSegmentGenerator fromConfig(QuoterConfig config) {
        return new NodeSegmentGenerator(
                ParameterNameCache.fromConfig(config),
                MethodCache.fromClassRef(NodeFactory.class)
        );
    }

    /**
     * Coverts a Node to a Segment(NonTerminal or a Terminal/Token).
     */
    public Segment createNodeSegment(Node node) {
        if (node == null) {
            return SegmentGenerator.createNullSegment();
        } else if (node instanceof Token) {
            return TokenSegmentGenerator.createTokenSegment((Token) node);
        } else if (node instanceof NonTerminalNode) {
            return nonTerminalSegmentGenerator.createSegment((NonTerminalNode) node);
        } else {
            throw new QuoterException("Expected non terminal or token. " +
                    "Found unexpected node type for: " + node);
        }
    }
}
