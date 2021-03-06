package io.ballerina.shell.jupyter;

import io.ballerina.shell.jupyter.kernel.BallerinaKernel;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * The entry point for Ballerina Shell Kernel.
 *
 * @since 2.0.0
 */
class IBallerinaImpl implements IBallerina {
    @Override
    public void run(Path connectionFile) throws Exception {
        // Create the jupyter connection.
        String contents = Files.readString(connectionFile, Charset.defaultCharset());
        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);
        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        // Create the connection instance and run it.
        BallerinaKernel kernel = new BallerinaKernel();
        kernel.becomeHandlerForConnection(connection);
        connection.connect();
        connection.waitUntilClose();
    }
}
