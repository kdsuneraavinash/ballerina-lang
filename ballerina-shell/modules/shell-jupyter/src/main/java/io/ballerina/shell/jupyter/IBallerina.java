package io.ballerina.shell.jupyter;

import java.nio.file.Path;

/**
 * The iBallerina interface.
 * Will provide methods to spin-up a kernel or run jupyter
 * commands directly.
 *
 * @since 2.0.0
 */
public interface IBallerina {
    static IBallerina create() {
        return new IBallerinaImpl();
    }

    /**
     * Run the jupyter Ballerina kernel via the connection file.
     * This is to be directly called to run the kernel which can be
     * connected to a jupyter or a similar client.
     * Additional setup might be needed on the client-end.
     *
     * @param connectionFile Connection file.
     * @throws Exception If running the shell/kernel failed.
     */
    void runJupyterKernel(Path connectionFile) throws Exception;

    /**
     * Pseudo jupyter command to be directly run via Ballerina.
     * This will be similar to `jupyter ***` call but the notebooks
     * or other calls will be using iballerina as the kernel.
     * On first run, this will install the iballerina kernel in jupyter.
     * However, for this call to success the user will have to have
     * other prerequisites already installed in the system.
     * <p>
     * This will directly run some shell commands to spin-up the
     * kernels and output instructions if it were to fail.
     *
     * @throws Exception If running the shell/kernel failed.
     */
    void jupyter(String... args) throws Exception;
}
