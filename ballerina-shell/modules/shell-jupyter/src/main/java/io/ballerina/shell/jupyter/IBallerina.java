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
    void run(Path connectionFile) throws Exception;
}
