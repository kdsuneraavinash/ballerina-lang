/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.syntax.tree;

/**
 * The {@code SyntaxNodeVisitor} visits each node in the syntax tree allowing
 * us to do something at each node.
 * <p>
 * This class separates tree nodes from various unrelated operations that needs
 * to be performed on the syntax tree nodes.
 * <p>
 * {@code SyntaxNodeVisitor} is a abstract class that itself visits the complete
 * tree. Subclasses have the ability to override only the required visit methods.
 * <p>
 * There exists a visit method for each node in the Ballerina syntax tree.
 * These methods return void. If you are looking for a visitor that has visit
 * methods that returns something, see {@link SyntaxNodeTransformer}.
 *
 * @see SyntaxNodeTransformer
 * @since 1.3.0
 */
public abstract class SyntaxNodeVisitor {
    public void visit(ModulePart modulePart) {
        visitSyntaxNode(modulePart);
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        visitSyntaxNode(functionDefinitionNode);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        visitSyntaxNode(typeDefinitionNode);
    }

    public void visit(ImportDeclaration importDeclaration) {
        visitSyntaxNode(importDeclaration);
    }

    // Statements

    public void visit(LocalVariableDeclaration localVariableDeclaration) {
        visitSyntaxNode(localVariableDeclaration);
    }

    public void visit(AssignmentStatement assignmentStatement) {
        visitSyntaxNode(assignmentStatement);
    }

    public void visit(BlockStatement blockStatement) {
        visitSyntaxNode(blockStatement);
    }

    public void visit(ReturnStatement returnStatement) {
        visitSyntaxNode(returnStatement);
    }

    // Expressions

    public void visit(BinaryExpression binaryExpression) {
        visitSyntaxNode(binaryExpression);
    }

    public void visit(FunctionCallNode functionCallNode) {
        visitSyntaxNode(functionCallNode);
    }

    public void visit(BracedExpression bracedExpression) {
        visitSyntaxNode(bracedExpression);
    }

    // Tokens

    public void visit(Token token) {
    }

    // Misc

    public void visit(EmptyNode emptyNode) {
    }

    public void visit(Minutiae minutiae) {
    }

    public void visit(RequiredParameter requiredParameter) {
        visitSyntaxNode(requiredParameter);
    }

    public void visit(PositionalArgumentNode positionalArgumentNode) {
        visitSyntaxNode(positionalArgumentNode);
    }

    public void visit(NamedArgumentNode namedArgumentNode) {
        visitSyntaxNode(namedArgumentNode);
    }

    public void visit(RestArgumentNode restArgumentNode) {
        visitSyntaxNode(restArgumentNode);
    }

    public void visit(ObjectFieldNode objectFieldNode) {
        visitSyntaxNode(objectFieldNode);
    }

    public void visit(RecordFieldNode recordFieldNode) {
        visitSyntaxNode(recordFieldNode);
    }

    public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        visitSyntaxNode(recordFieldWithDefaultValueNode);
    }

    public void visit(RecordRestDescriptorNode recordRestDescriptorNode) {
        visitSyntaxNode(recordRestDescriptorNode);
    }

    public void visit(NodeList nodeList) {
        visitSyntaxNode(nodeList);
    }

    public void visit(RecordTypeDescriptorNode recordTypeDescriptorNode) {
        visitSyntaxNode(recordTypeDescriptorNode);
    }

    public void visit(ObjectTypeDescriptorNode objectTypeDescriptorNode) {
        visitSyntaxNode(objectTypeDescriptorNode);
    }

    public void visit(TypeReferenceNode typeReferenceNode) {
        visitSyntaxNode(typeReferenceNode);
    }


    protected void visitSyntaxNode(Node node) {
        // TODO Find a better way to check for token
        if (node instanceof Token) {
            node.accept(this);
            return;
        }

        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        int bucketCount = nonTerminalNode.bucketCount();
        for (int bucket = 0; bucket < bucketCount; bucket++) {
            Node child = nonTerminalNode.childInBucket(bucket);
            child.accept(this);
        }
    }
}
