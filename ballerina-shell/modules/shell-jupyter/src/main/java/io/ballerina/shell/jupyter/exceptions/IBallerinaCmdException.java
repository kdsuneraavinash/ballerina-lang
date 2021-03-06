package io.ballerina.shell.jupyter.exceptions;

import io.ballerina.shell.exceptions.BallerinaShellException;

/**
 * An exception occurring because of some error on kernel side.
 *
 * @since 2.0.0
 */
public class IBallerinaCmdException extends BallerinaShellException {
    public IBallerinaCmdException(String message) {
        super(message);
    }
}
