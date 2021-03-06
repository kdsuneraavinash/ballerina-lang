package io.ballerina.shell.jupyter;

import com.google.gson.Gson;
import io.ballerina.shell.jupyter.jupyter.json.KernelSpec;
import io.ballerina.shell.jupyter.jupyter.json.KernelSpecJson;
import io.ballerina.shell.jupyter.kernel.BallerinaKernel;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.KernelConnectionProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * The entry point for Ballerina Shell Kernel.
 */
class IBallerinaImpl implements IBallerina {
    private static final String BALLERINA_KERNEL_NAME = "iballerina";
    private static final String SPECIAL_DELIMITER = "\\A";

    private static void recursiveDeleteOnShutdownHook(final Path path) {
        Thread fileDeleter = new Thread(() -> {
            try {
                Files.walkFileTree(path, new RecursiveDeleter<>());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Runtime.getRuntime().addShutdownHook(fileDeleter);
    }

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
                    "Seems like you don't have jupyter installed correctly.\n" +
                    "Please follow instructions at: https://jupyter.org/install to install jupyter correctly.");
        }

        if (resourceDirectory != null) {
            outStream.println("Ballerina kernel is already installed in " + resourceDirectory);
        } else {
            outStream.println("Ballerina kernel not found. Installing...");
            try {
                // Create Ballerina kernel spec.
                String balCommand = Paths.get(System.getProperty("ballerina.home"))
                        .resolve("bin").resolve("bal").toAbsolutePath().toString();
                KernelSpec iBallerinaKernelSpec = new KernelSpec();
                iBallerinaKernelSpec.setName(BALLERINA_KERNEL_NAME);
                iBallerinaKernelSpec.setDisplayName(BALLERINA_KERNEL_NAME);
                iBallerinaKernelSpec.setLanguage("ballerina");
                iBallerinaKernelSpec.setArgv(List.of(balCommand, "jupyter", "-f", "{connection_file}"));
                String iBallerinaJson = gson.toJson(iBallerinaKernelSpec);

                // Have to install Ballerina kernel.
                // Create a temporary json file containing all the
                // required kernel information in a temporary directory.
                // The file path would be /tmp/TEMP_DIRECTORY/iballerina/kernel.json
                Path tmpDirectory = Files.createTempDirectory("jupyter-iballerina-");
                recursiveDeleteOnShutdownHook(tmpDirectory);
                Path kernelDirectory = tmpDirectory.resolve(BALLERINA_KERNEL_NAME);
                Files.createDirectory(kernelDirectory);
                Path kernelFile = kernelDirectory.resolve("kernel.json");
                try (FileWriter fileWriter = new FileWriter(kernelFile.toFile())) {
                    fileWriter.write(iBallerinaJson);
                }
                runCommand("jupyter", "kernelspec", "install", "--user",
                        kernelDirectory.toAbsolutePath().toString());
            } catch (Exception e) {
                throw new IBallerinaException("" +
                        "Kernel installation failed: " + e.toString() + "\n" +
                        "Seems like you don't have jupyter/ballerina installed correctly.\n" +
                        "Please follow instructions at: https://jupyter.org/install to install jupyter.");
            }
        }

        // Output instructions to run.
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

    private static class RecursiveDeleter<T extends Path> extends SimpleFileVisitor<T> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e)
                throws IOException {
            if (e == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
            throw e;
        }
    }
}
