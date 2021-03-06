package io.ballerina.shell.jupyter.magics;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager that will handle all the magic commands.
 *
 * @since 2.0.0
 */
public class MagicCommandManager {
    private final List<MagicCommand<?>> magicCommands;

    public MagicCommandManager() {
        this.magicCommands = new ArrayList<>();
    }

    /**
     * Whether the given args should be handled by the command manager.
     * If this returns true, it is safe to all {@code handle(args)}.
     */
    public boolean shouldHandle(String... args) {
        return magicCommands.stream().anyMatch(mc -> mc.shouldHandle(args));
    }

    /**
     * Handles the given arguments as if they were magic commands.
     * If they are not magic commands that have a handler, {@link UnsupportedOperationException}
     * will be thrown.
     */
    public String handle(String... args) throws Exception {
        for (MagicCommand<?> magicCommand : magicCommands) {
            if (magicCommand.shouldHandle(args)) {
                return magicCommand.accept(args);
            }
        }
        throw new UnsupportedOperationException("Command unsupported");
    }

    public void register(MagicCommand<?> magicCommand) {
        magicCommands.add(magicCommand);
    }

    public void remove(MagicCommand<?> magicCommand) {
        magicCommands.remove(magicCommand);
    }
}
