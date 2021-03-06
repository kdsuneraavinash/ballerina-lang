package io.ballerina.shell.jupyter;

import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * The entry point for Ballerina Shell Kernel.
 */
public class IBallerinaRunner {
    private static BallerinaKernel kernel = null;

    /**
     * Obtain a reference to the kernel created by running {@link #main(String[])}. This
     * kernel may be null if one is not present but as the main use for this method is
     * for the kernel user code to access kernel services.
     *
     * @return the kernel created by running {@link #main(String[])} or {@code null} if
     * one has not yet (or already created and finished) been created.
     */
    public static BallerinaKernel getKernelInstance() {
        return IBallerinaRunner.kernel;
    }

    public static void main(String[] args) throws Exception {
        // The first argument must contain the connection file
        // required to connect.
        if (args.length < 1) {
            throw new IllegalArgumentException("Missing connection file argument");
        }
        Path connectionFile = Paths.get(args[0]);
        if (!Files.isRegularFile(connectionFile)) {
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");
        }

        // Create the jupyter connection.
        String contents = new String(Files.readAllBytes(connectionFile));
        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);
        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        // Create the connection instance.
        kernel = new BallerinaKernel();
        kernel.becomeHandlerForConnection(connection);
        connection.connect();
        connection.waitUntilClose();

        // Dispose of kernel.
        kernel = null;
        System.exit(0);
    }
}
