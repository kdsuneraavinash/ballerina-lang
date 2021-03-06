package io.ballerina.shell.jupyter.exceptions;

import io.ballerina.shell.exceptions.BallerinaShellException;

import java.util.List;

/**
 * An exception occurring because of some error on ballerina shell.
 *
 * @since 2.0.0
 */
public class IBallerinaException extends BallerinaShellException {
    private final List<String> errorDiagnostics;

    public IBallerinaException(List<String> errorDiagnostics, Throwable e) {
        super(e);
        this.errorDiagnostics = errorDiagnostics;
    }

    public List<String> getErrorDiagnostics() {
        return errorDiagnostics;
    }
}
