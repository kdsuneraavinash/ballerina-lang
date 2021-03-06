package io.ballerina.shell.jupyter;

import com.google.gson.Gson;
import io.ballerina.shell.jupyter.exceptions.IBallerinaCmdException;
import io.ballerina.shell.jupyter.jupyter.json.KernelSpec;
import io.ballerina.shell.jupyter.jupyter.json.KernelSpecJson;
import io.ballerina.shell.jupyter.utils.RecursiveDeleterThread;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * The implementation of {@link IBallerinaInstaller}.
 *
 * @since 2.0.0
 */
class IBallerinaInstallerImpl extends IBallerinaInstaller {
    private static final String SPECIAL_DELIMITER = "\\A";
    private final PrintStream outStream;
    private final Gson gson;

    IBallerinaInstallerImpl(PrintStream outStream) {
        this.outStream = outStream;
        this.gson = new Gson();
    }

    @Override
    public void preKernelInstall() throws IBallerinaCmdException {
        try {
            runCommand("jupyter", "--version");
        } catch (Exception e) {
            throw new IBallerinaCmdException("" +
                    "Command failed because: " + e.getMessage() + "\n" +
                    "Seems like you don't have jupyter installed. " +
                    "Running this command requires you to install jupyter.\n" +
                    "Follow instructions at: https://jupyter.org/install " +
                    "to install jupyter correctly.");
        }
    }

    @Override
    public boolean isKernelInstalled() throws IBallerinaCmdException {
        try {
            InputStream jupyterKernels = runCommand("jupyter", "kernelspec", "list", "--json");
            String jupyterKernelsStringJson = getStreamContent(jupyterKernels);
            KernelSpecJson kernelSpecJson = this.gson.fromJson(jupyterKernelsStringJson, KernelSpecJson.class);
            if (kernelSpecJson.getKernelSpecDataMap().containsKey(BALLERINA_KERNEL_NAME)) {
                String resourceDir = kernelSpecJson.getKernelSpecDataMap()
                        .get(BALLERINA_KERNEL_NAME).getResourceDir();
                return Files.exists(Paths.get(resourceDir));
            }
        } catch (IOException | InterruptedException e) {
            throw new IBallerinaCmdException("" +
                    "Command failed because: " + e.getMessage() + "\n" +
                    "Seems like you don't have jupyter installed correctly.\n" +
                    "Please follow instructions at: https://jupyter.org/install to install jupyter correctly.");
        }
        return false;
    }

    @Override
    public void kernelInstall() throws IBallerinaCmdException {
        try {
            outStream.println("Installing Ballerina jupyter kernel...");

            // Create Ballerina kernel spec.
            String balCommand = Paths.get(System.getProperty("ballerina.home"))
                    .resolve("bin").resolve("bal").toAbsolutePath().toString();
            KernelSpec iBallerinaKernelSpec = new KernelSpec();
            iBallerinaKernelSpec.setName(BALLERINA_KERNEL_NAME);
            iBallerinaKernelSpec.setDisplayName(BALLERINA_KERNEL_NAME);
            iBallerinaKernelSpec.setLanguage("ballerina");
            iBallerinaKernelSpec.setArgv(List.of(balCommand, "jupyter", "-f", "{connection_file}"));
            String iBallerinaJson = this.gson.toJson(iBallerinaKernelSpec);

            // Have to install Ballerina kernel.
            // Create a temporary json file containing all the
            // required kernel information in a temporary directory.
            // The file path would be /tmp/TEMP_DIRECTORY/iballerina/kernel.json
            Path tmpDirectory = Files.createTempDirectory("jupyter-iballerina-");
            Runtime.getRuntime().addShutdownHook(new RecursiveDeleterThread(tmpDirectory));
            Path kernelDirectory = tmpDirectory.resolve(BALLERINA_KERNEL_NAME);
            Files.createDirectory(kernelDirectory);
            Path kernelFile = kernelDirectory.resolve("kernel.json");
            try (FileWriter fileWriter = new FileWriter(kernelFile.toFile(), Charset.defaultCharset())) {
                fileWriter.write(iBallerinaJson);
            }
            runCommand("jupyter", "kernelspec", "install", "--user",
                    kernelDirectory.toAbsolutePath().toString());
        } catch (IOException | InterruptedException e) {
            throw new IBallerinaCmdException("" +
                    "Kernel installation failed: " + e.toString() + "\n" +
                    "Seems like you don't have jupyter/ballerina installed correctly.\n" +
                    "Please follow instructions at: https://jupyter.org/install to install jupyter.");
        }
    }

    @Override
    public void postKernelInstall() {
        outStream.println("Ballerina kernel is correctly set-up.");
        outStream.println("Use following commands to start the jupyter client,");
        outStream.println("\tTo start the notebook instance,");
        outStream.println("\t\t$ jupyter notebook");
        outStream.println("\tGo to the jupyter client and select New > " + BALLERINA_KERNEL_NAME +
                " to start a new instance.");
        outStream.println();
        outStream.println("\tTo start the jupyter console,");
        outStream.println("\t\t$ jupyter console --kernel " + BALLERINA_KERNEL_NAME);
        outStream.println();
        outStream.println("Refer https://jupyter.org/ for more instructions on using jupyter.");
        outStream.println("Refer https://ballerina.io/ to learn more about Ballerina.");
    }

    /**
     * Runs a command and outputs its output data as a input stream.
     * Note that this method will block until the process has exit.
     * Also, this will run via exec, meaning that aliases will be available to run.
     *
     * @param command Command to run.
     * @return Output of the command execution.
     * @throws IOException If execution failed or exit with non-zero exit code.
     */
    private InputStream runCommand(String... command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        if (process.exitValue() != 0) {
            String commandString = String.join(" ", command);
            throw new IOException(commandString + " command exited with exit code " +
                    process.exitValue());
        }
        return process.getInputStream();
    }

    /**
     * Get the content of the input stream as a string.
     *
     * @param inputStream Input stream to read.
     * @return Stream content.
     */
    private String getStreamContent(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream,
                Charset.defaultCharset()).useDelimiter(SPECIAL_DELIMITER);
        return scanner.next();
    }
}
