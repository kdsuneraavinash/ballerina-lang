package io.ballerina.shell.jupyter;

import io.ballerina.shell.Evaluator;
import io.ballerina.shell.EvaluatorBuilder;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;

/**
 * The Ballerina Kernel base class.
 */
public class BallerinaKernel extends BaseKernel {
    private final Evaluator evaluator;

    public BallerinaKernel() throws BallerinaShellException {
        this.evaluator = new EvaluatorBuilder().build();
        evaluator.initialize();
    }

    @Override
    public DisplayData eval(String expr) throws Exception {
        return new DisplayData(directEval(expr));
    }

    @Override
    public LanguageInfo getLanguageInfo() {
        return new LanguageInfo.Builder("ballerina")
                .fileExtension(".bal")
                .build();
    }

    /**
     * Evaluates the string expression given and returns the result if any.
     *
     * @param expr Expression to evaluate.
     * @return Expression evaluation result.
     * @throws Exception If expression contains errors.
     */
    protected String directEval(String expr) throws Exception {
        return evaluator.evaluate(expr);
    }
}
