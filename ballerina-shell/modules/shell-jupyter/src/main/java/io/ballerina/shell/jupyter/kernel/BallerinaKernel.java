package io.ballerina.shell.jupyter.kernel;

import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.DiagnosticKind;
import io.ballerina.shell.Evaluator;
import io.ballerina.shell.EvaluatorBuilder;
import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.jupyter.exceptions.IBallerinaException;
import io.github.spencerpark.jupyter.kernel.BaseKernel;
import io.github.spencerpark.jupyter.kernel.LanguageInfo;
import io.github.spencerpark.jupyter.kernel.display.DisplayData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The Ballerina Kernel base class.
 * This will be the kernel that will connect to shell core to
 * run the expression evaluations.
 *
 * @since 2.0.0
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
        try {
            return evaluator.evaluate(expr);
        } catch (BallerinaShellException e) {
            if (evaluator.hasErrors()) {
                List<String> diagnostics = evaluator.diagnostics().stream()
                        .filter(d -> DiagnosticKind.ERROR.equals(d.getKind()))
                        .map(Diagnostic::toString)
                        .collect(Collectors.toList());
                evaluator.resetDiagnostics();
                throw new IBallerinaException(diagnostics, e);
            }
            throw e;
        }
    }

    @Override
    public List<String> formatError(Exception e) {
        if (e instanceof IBallerinaException) {
            IBallerinaException iBallerinaException = (IBallerinaException) e;
            return iBallerinaException.getErrorDiagnostics().stream()
                    .map(errorStyler::secondary)
                    .collect(Collectors.toList());
        }
        return super.formatError(e);
    }
}
