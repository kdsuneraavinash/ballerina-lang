/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.cli.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.shell.jupyter.IBallerina;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.Objects;

import static io.ballerina.cli.cmd.Constants.JUPYTER_COMMAND;

/**
 * This class represents the "ballerina run-kernel" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = JUPYTER_COMMAND, description = "Run ballerina Jupyter kernel services")
public class JupyterCommand implements BLauncherCmd {
    private PrintStream errStream;

    @CommandLine.ArgGroup(exclusive = false)
    private KernelModeGroups kernelModeGroups;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Parameters(description = "CLI arguments to jupyter command.")
    private String[] args;

    public JupyterCommand() {
        errStream = System.err;
    }

    public JupyterCommand(PrintStream errStream) {
        this.errStream = errStream;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(JUPYTER_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }

        try {
            IBallerina iBallerina = IBallerina.create();
            if (kernelModeGroups != null && kernelModeGroups.kernelMode) {
                Objects.requireNonNull(kernelModeGroups.file, "Connection file parameter is required");
                iBallerina.runJupyterKernel(kernelModeGroups.file.toPath());
                return;
            }

            iBallerina.jupyter(args);
        } catch (Exception e) {
            errStream.println(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return JUPYTER_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Run ballerina Jupyter kernel services");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal jupyter [-k|--kernel-mode -f|--file <file-name>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private static class KernelModeGroups {
        @CommandLine.Option(names = {"-k", "--kernel-mode"}, description = "Whether to only run the kernel.")
        private boolean kernelMode = false;

        @CommandLine.Option(names = {"-f", "--file"}, description = "Connection file path.", required = true)
        private File file;
    }
}
