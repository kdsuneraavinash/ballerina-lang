package io.ballerina.shell.jupyter;

import com.google.gson.Gson;
import io.ballerina.shell.jupyter.jupyter.json.KernelSpecJson;
import io.ballerina.shell.jupyter.kernel.BallerinaKernel;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * The entry point for Ballerina Shell Kernel.
 */
class IBallerinaImpl implements IBallerina {
    private static final String BALLERINA_KERNEL_NAME = "iballerina";
    private static final String SPECIAL_DELIMITER = "\\A";

    @Override
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
    public void install(PrintStream outStream) throws Exception {
        // Check if the jupyter is setup correctly
        try {
            runCommand("jupyter", "--version");
        } catch (Exception e) {
            throw new IBallerinaException("" +
                    "Command failed because: " + e.getMessage() + "\n" +
                    "Seems like you don't have jupyter installed. " +
                    "Running this command requires you to install jupyter.\n" +
                    "Follow instructions at: https://jupyter.org/install " +
                    "to install jupyter correctly.");
        }

        Gson gson = new Gson();
        String resourceDirectory = null;

        // Check if iballerina is already set-up.
        try {
            InputStream jupyterKernels = runCommand("jupyter", "kernelspec", "list", "--json");
            String jupyterKernelsStringJson = getStreamContent(jupyterKernels);
            KernelSpecJson kernelSpecJson = gson.fromJson(jupyterKernelsStringJson, KernelSpecJson.class);
            if (kernelSpecJson.getKernelSpecDataMap().containsKey(BALLERINA_KERNEL_NAME)) {
                resourceDirectory = kernelSpecJson.getKernelSpecDataMap()
                        .get(BALLERINA_KERNEL_NAME).getResourceDir();
            }
        } catch (Exception e) {
            throw new IBallerinaException("" +
                    "Command failed because: " + e.getMessage() + "\n" +
                    "Seems like you don't have jupyter installed correctly. " +
                    "Please follow instructions at: https://jupyter.org/install " +
                    "to install jupyter correctly.");
        }

        if (resourceDirectory != null) {
            outStream.println("Ballerina kernel is already installed in " + resourceDirectory);
        } else {
            outStream.println("Ballerina kernel not found. Installing...");
            // Have to install Ballerina kernel.
            // Create a temporary json file containing all the
            // required kernel information in a temporary directory.
            // The file path would be /tmp/TEMP_DIRECTORY/iballerina/kernel.json
            // TODO: Install kernel named BALLERINA_KERNEL_NAME
        }

        outStream.println("Ballerina kernel is correctly set-up.");
        outStream.println("Use following commands to start the jupyter client,");
        outStream.println("\tTo start the notebook instance, (select " + BALLERINA_KERNEL_NAME + " from new)");
        outStream.println("\t\t$ jupyter notebook");
        outStream.println("\tTo start the jupyter console,");
        outStream.println("\t\t$ jupyter console --kernel " + BALLERINA_KERNEL_NAME);
        outStream.println();
        outStream.println("Refer https://jupyter.org/ for more instructions on using jupyter.");
        outStream.println("Refer https://ballerina.io/ to learn more about Ballerina.");
    }

    private InputStream runCommand(String... command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        if (process.exitValue() != 0) {
            String commandString = String.join(" ", command);
            throw new IOException("[" + commandString + "] command exited with " +
                    process.exitValue());
        }
        return process.getInputStream();
    }

    private String getStreamContent(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream,
                Charset.defaultCharset()).useDelimiter(SPECIAL_DELIMITER)) {
            return scanner.next();
        }
    }
}
