package io.ballerina.shell.jupyter.exceptions;

import io.ballerina.shell.Diagnostic;
import io.ballerina.shell.exceptions.BallerinaShellException;

import java.util.List;

/**
 * An exception occurring because of some error on ballerina shell.
 *
 * @since 2.0.0
 */
public class IBallerinaException extends BallerinaShellException {
    private final List<Diagnostic> diagnostics;

    public IBallerinaException(List<Diagnostic> diagnostics, Throwable e) {
        super(e);
        this.diagnostics = diagnostics;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
