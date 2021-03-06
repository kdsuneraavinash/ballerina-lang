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

import static io.ballerina.cli.cmd.Constants.JUPYTER_COMMAND;

/**
 * This class represents the "ballerina run-kernel" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = JUPYTER_COMMAND, description = "Install/Run ballerina jupyter kernel")
public class JupyterCommand implements BLauncherCmd {
    private PrintStream errStream;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-f", "--file"}, description = "Connection file path.")
    private File file;

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
            if (file != null) {
                iBallerina.runJupyterKernel(file.toPath());
                return;
            }

            iBallerina.install(errStream);
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
        out.append("Install/Run ballerina jupyter kernel");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal jupyter [-f|--file <file-name>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
