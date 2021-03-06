package io.ballerina.shell.jupyter.magics;

/**
 * Abstract class for magic commands such as %reset.
 *
 * @param <T> The class type that this magic can be performed on.
 * @since 2.0.0
 */
public abstract class MagicCommand<T> {
    protected final T receiver;
    private final String command;

    protected MagicCommand(T receiver, String command) {
        this.receiver = receiver;
        this.command = command;
    }

    /**
     * Whether this command should handle the given arguments.
     * If this returns true, you can call {@code accept(args)} to perform magic.
     */
    public boolean shouldHandle(String... args) {
        return args.length > 0 && command.equals(args[0].strip());
    }

    /**
     * Performs this magic command. The arguments should contain all the
     * arguments, including the magic command prefix.
     */
    public abstract String accept(String... args) throws Exception;
}
