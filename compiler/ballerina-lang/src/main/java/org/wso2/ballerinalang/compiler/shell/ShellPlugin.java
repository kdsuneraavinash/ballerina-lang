/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.shell;

import org.ballerinalang.compiler.CompilerPhase;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.SHELL_MODE;

/**
 * Performs processing required by the Ballerina Shell.
 * Following operations are done by this plugin,
 * <ul>
 *     <li> Replace all assignments to global variables with MEM("a", a) calls.
 *     <li> Replace all global variable uses with (Type) REM("a") calls.
 * </ul>
 *
 * @since 2.0.0
 */
public class ShellPlugin extends NodeRewriter {
    private static final CompilerContext.Key<ShellPlugin> SHELL_PLUGIN_KEY = new CompilerContext.Key<>();
    private static final String INIT_FUNCTION_PREFIX = "..<init>";
    private static final String RECALL_ANY_NAME = "__recall_any";
    private static final String RECALL_ANY_ERROR_NAME = "__recall_any_error";
    private static final String MEMORIZE_NAME = "__memorize";
    private static final String CONTEXT_ID_NAME = "__last__";
    private static final String DOLLAR = "$";

    private final SymbolTable symTable;
    private final Types types;
    private final Boolean inShellMode;
    private BInvokableSymbol recallAnyFunction;
    private BInvokableSymbol recallAnyErrorFunction;
    private BInvokableSymbol memorizeFunction;

    private ShellPlugin(CompilerContext context) {
        context.put(SHELL_PLUGIN_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        CompilerOptions options = CompilerOptions.getInstance(context);
        this.inShellMode = options.isSet(SHELL_MODE)
                && Boolean.parseBoolean(options.get(SHELL_MODE));
    }

    public static ShellPlugin getInstance(CompilerContext context) {
        ShellPlugin shellPlugin = context.get(SHELL_PLUGIN_KEY);
        if (shellPlugin == null) {
            shellPlugin = new ShellPlugin(context);
        }

        return shellPlugin;
    }

    /**
     * Performs operations to prepare package to the shell execution.
     * Skips if {@code shellCode} flag is not set.
     *
     * @param pkgNode Package node to process.
     * @return Processed package node.
     */
    public BLangPackage perform(BLangPackage pkgNode) {
        if (!this.inShellMode) {
            return pkgNode;
        }
        return rewrite(pkgNode);
    }

    /**
     * Generates invocation for the MEM call.
     * The generated call will effectively store variable with the value.
     *
     * @param variable   Variable to generate the MEM call.
     * @param expression Expression that should be stored in MEM call.
     *                   This will be rewritten.
     * @return Generated statement.
     */
    private BLangExpressionStmt memorizeInvocation(BLangSimpleVarRef.BLangPackageVarRef variable,
                                                   BLangExpression expression) {
        // Prepare parameters of the invocation
        ArrayList<BLangExpression> parameters = new ArrayList<>();
        parameters.add(NodeUtils.createStringLiteral(symTable, variable.varSymbol.name.value));
        parameters.add(NodeUtils.createTypeCastExpr(expression, symTable.anyOrErrorType));

        // Statement with the memorize invocation: MEM(a, b)
        BLangExpression memorizeExpr = NodeUtils.createInvocation(memorizeFunction, parameters);
        return NodeUtils.createStatement(memorizeExpr);
    }

    /**
     * Generates invocation for the REM call.
     * The generated call will effectively return the variable value.
     *
     * @param variable Variable to generate REM call to.
     * @return Generated expression that returns the variable value.
     */
    private BLangExpression recallInvocation(BLangSimpleVarRef.BLangPackageVarRef variable) {
        // Decide on recall function to use
        BInvokableSymbol invokableSymbol = types.containsErrorType(variable.type)
                ? recallAnyErrorFunction : recallAnyFunction;

        // Prepare parameters of the invocation
        ArrayList<BLangExpression> parameters = new ArrayList<>();
        parameters.add(NodeUtils.createStringLiteral(symTable, variable.varSymbol.name.value));

        // Cast and return expression
        BLangExpression expression = NodeUtils.createInvocation(invokableSymbol, parameters);
        return NodeUtils.createTypeCastExpr(expression, variable.type);
    }

    /**
     * All the package variables that don't have a $ are processed.
     * CONTEXT_ID variable is also skipped.
     *
     * @return Whether to process this package variable.
     */
    private boolean shouldProcessPkgVariable(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        String packageVarRefName = packageVarRef.symbol.name.value;
        boolean shouldSkip = packageVarRef.internal || packageVarRefName.contains(DOLLAR)
                || packageVarRefName.equals(CONTEXT_ID_NAME);
        return !shouldSkip;
    }

    /**
     * @return whether the expression is a package variable reference.
     */
    private boolean isProcessablePkgVariable(BLangExpression varRef) {
        if (varRef instanceof BLangSimpleVarRef.BLangPackageVarRef) {
            return shouldProcessPkgVariable((BLangSimpleVarRef.BLangPackageVarRef) varRef);
        }
        return false;
    }

    /**
     * All the functions, except for ones starting with
     * {@code ..<init>} are processed.
     *
     * @return Whether to process this function.
     */
    private boolean shouldProcessFunction(BLangFunction function) {
        return !function.name.value.startsWith(INIT_FUNCTION_PREFIX);
    }

    /**
     * Finds the required RECALL/MEM functions from the given
     * functions.
     *
     * @return If all functions were found.
     */
    private boolean storeRecallMemFunctions(List<BLangFunction> functions) {
        for (BLangFunction function : functions) {
            switch (function.name.value) {
                case RECALL_ANY_NAME:
                    recallAnyFunction = function.symbol;
                    break;
                case RECALL_ANY_ERROR_NAME:
                    recallAnyErrorFunction = function.symbol;
                    break;
                case MEMORIZE_NAME:
                    memorizeFunction = function.symbol;
                    break;
                default:
            }
        }
        return recallAnyFunction != null && recallAnyErrorFunction != null
                && memorizeFunction != null;
    }

    /**
     * Rewrite generated init functions. The package variables defined inside
     * would be appended to the end of function as MEMORIZE calls.
     *
     * @param initFunction Init function to process.
     */
    public void rewriteInitFunction(BLangFunction initFunction) {
        if (initFunction == null) {
            return;
        }
        if (!(initFunction.body instanceof BLangBlockFunctionBody)) {
            return;
        }

        // Find all the variables initialized inside the init.
        BLangBlockFunctionBody initFunctionBody = (BLangBlockFunctionBody) initFunction.body;
        Set<BLangSimpleVarRef.BLangPackageVarRef> varRefs = initFunctionBody.stmts.stream()
                .filter(stmt -> stmt instanceof BLangAssignment)
                .map(stmt -> (BLangAssignment) stmt)
                .filter(bLangAssignment -> isProcessablePkgVariable(bLangAssignment.varRef))
                .map(bLangAssignment -> (BLangSimpleVarRef.BLangPackageVarRef) bLangAssignment.varRef)
                .collect(Collectors.toSet());

        // Add all MEMORIZE calls at the end of the body, just before return statement.
        BLangStatement returnStmt = initFunctionBody.stmts.remove(initFunctionBody.stmts.size() - 1);
        for (BLangSimpleVarRef.BLangPackageVarRef varRef : varRefs) {
            initFunctionBody.addStatement(memorizeInvocation(varRef, varRef));
        }
        initFunctionBody.addStatement(returnStmt);
    }

    // Visitor overrides

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.SHELL_PLUGIN)) {
            // Already shell phase was executed on the node.
            return;
        }

        if (!storeRecallMemFunctions(pkgNode.functions)) {
            // Required functions not found.
            return;
        }

        // Rewrite init functions (.<init> and ..<init>N) to add memorize("a", a) to the end
        // for any global variable that is initialized in the init function
        // since that change will not be reflected in the global memory.
        rewriteInitFunction(pkgNode.initFunction);
        for (BLangFunction function : pkgNode.functions) {
            if (function.name.value.startsWith(INIT_FUNCTION_PREFIX)) {
                rewriteInitFunction(function);
            }
        }

        super.visit(pkgNode);
        pkgNode.completedPhases.add(CompilerPhase.SHELL_PLUGIN);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        // Replace assignments with MEM("x", expr) nodes
        if (isProcessablePkgVariable(assignNode.varRef)) {
            BLangSimpleVarRef.BLangPackageVarRef varRef = (BLangSimpleVarRef.BLangPackageVarRef) assignNode.varRef;
            result = memorizeInvocation(varRef, rewrite(assignNode.expr));
            return;
        }
        super.visit(assignNode);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        if (shouldProcessPkgVariable(packageVarRef)) {
            result = recallInvocation(packageVarRef);
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (shouldProcessFunction(funcNode)) {
            super.visit(funcNode);
        }
    }
}
