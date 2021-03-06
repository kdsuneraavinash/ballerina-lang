package io.ballerina.shell.jupyter.magics.impl;

import io.ballerina.shell.exceptions.BallerinaShellException;

/**
 * Denotes that this is able to reset.
 * Can apply {@link ResetMagic}.
 *
 * @since 2.0.0
 */
public interface Resettable {
    void reset() throws BallerinaShellException;
}
