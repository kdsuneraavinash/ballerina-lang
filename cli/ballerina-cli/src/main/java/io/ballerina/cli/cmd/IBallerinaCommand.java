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

import static io.ballerina.cli.cmd.Constants.RUN_KERNEL_COMMAND;

/**
 * This class represents the "ballerina run-kernel" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = RUN_KERNEL_COMMAND, description = "Run ballerina Jupyter kernel")
public class IBallerinaCommand implements BLauncherCmd {
    private PrintStream errStream;

    @CommandLine.Option(names = {"--help", "-h", "?"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-f", "--file"}, description = "Connection file path.", required = true)
    private File file;

    public IBallerinaCommand() {
        errStream = System.err;
    }

    public IBallerinaCommand(PrintStream errStream) {
        this.errStream = errStream;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(RUN_KERNEL_COMMAND);
            errStream.println(commandUsageInfo);
            return;
        }
        try {
            IBallerina.run(file.toPath());
        } catch (Exception e) {
            errStream.println("Could not start the Jupyter IBallerina kernel: " + e.toString());
        }
    }

    @Override
    public String getName() {
        return RUN_KERNEL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Run ballerina Jupyter kernel");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal run-kernel [-f|--file] <connection-file>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
