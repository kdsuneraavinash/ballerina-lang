package io.ballerina.shell.jupyter;

import io.ballerina.shell.jupyter.kernel.BallerinaKernel;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * The entry point for Ballerina Shell Kernel.
 */
class IBallerinaImpl implements IBallerina {
    public void runJupyterKernel(Path connectionFile) throws Exception {
        // Create the jupyter connection.
        String contents = new String(Files.readAllBytes(connectionFile));
        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);
        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        // Create the connection instance and run it.
        BallerinaKernel kernel = new BallerinaKernel();
        kernel.becomeHandlerForConnection(connection);
        connection.connect();
        connection.waitUntilClose();
    }

    @Override
    public void jupyter(String... args) throws Exception {

    }
}
