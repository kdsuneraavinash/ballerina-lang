package io.ballerina.shell.jupyter;

import java.io.PrintStream;

/**
 * An installer for iBallerina.
 * Will guide the user to setup jupyter/ballerina.
 *
 * @since 2.0.0
 */
public abstract class IBallerinaInstaller {
    protected static final String BALLERINA_KERNEL_NAME = "iballerina";

    public static IBallerinaInstaller create(PrintStream outStream) {
        return new IBallerinaInstallerImpl(outStream);
    }

    /**
     * On first run, this will install the iballerina kernel in jupyter.
     * However, for this call to success the user will have to have
     * other prerequisites already installed in the system.
     * If already installed, this will output instructions on
     * how to start the instance.
     * <p>
     * This will directly run some shell commands to spin-up the
     * kernels and output instructions if it were to fail.
     *
     * @throws Exception If running the shell/kernel failed.
     */
    public void install() throws Exception {
        preKernelInstall();
        if (!isKernelInstalled()) {
            kernelInstall();
        }
        postKernelInstall();
    }

    /**
     * This does any checks required prior to kernel install.
     * For example, this may check if jupyter is installed.
     * If any of the prerequisites are not fulfilled, this call will fail.
     *
     * @throws Exception If requirements are not satisfied.
     */
    protected abstract void preKernelInstall() throws Exception;

    /**
     * This will check whether the jupyter ballerina kernel is already installed.
     * This will use jupyter commands to check and it is required to run
     * {@code preKernelInstall} prior to calling this method.
     *
     * @return Whether the kernel is already there.
     * @throws Exception If something went wrong with process calls.
     */
    protected abstract boolean isKernelInstalled() throws Exception;

    /**
     * Installs the jupyter ballerina kernel. This may create any files/directories
     * necessary for installation. It is required to run
     * {@code preKernelInstall} prior to calling this method.
     *
     * @throws Exception If something went wrong with process calls.
     */
    protected abstract void kernelInstall() throws Exception;

    /**
     * Runs anything that should be done after installation.
     * Generally this should display instructions for the user
     * to initiate a client to use this service.
     */
    protected abstract void postKernelInstall();
}
