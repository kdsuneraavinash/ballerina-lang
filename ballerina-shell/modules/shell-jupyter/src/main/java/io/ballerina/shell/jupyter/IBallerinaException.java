package io.ballerina.shell.jupyter;

import io.ballerina.shell.exceptions.BallerinaShellException;

/**
 * An exception occurring because of some error on kernel side.
 */
public class IBallerinaException extends BallerinaShellException {
    public IBallerinaException(String message) {
        super(message);
    }
}
