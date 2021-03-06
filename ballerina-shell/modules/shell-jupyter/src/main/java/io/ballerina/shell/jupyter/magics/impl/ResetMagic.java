package io.ballerina.shell.jupyter.magics.impl;

import io.ballerina.shell.exceptions.BallerinaShellException;
import io.ballerina.shell.jupyter.magics.MagicCommand;

import java.util.Objects;

/**
 * The magic command to reset session.
 * The command syntax is {@code %reset}.
 * This will reset the kernel session.
 *
 * @since 2.0.0
 */
public class ResetMagic extends MagicCommand<Resettable> {
    public ResetMagic(Resettable resettable) {
        super(resettable, "%reset");
    }

    @Override
    public String accept(String... strings) throws BallerinaShellException {
        Objects.requireNonNull(receiver).reset();
        return null;
    }
}
