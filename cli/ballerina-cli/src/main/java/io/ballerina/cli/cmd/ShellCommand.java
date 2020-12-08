package io.ballerina.cli.cmd;

import io.ballerina.shell.cli.Configuration;
import io.ballerina.shell.cli.ReplShellApplication;
import org.ballerinalang.tool.BLauncherCmd;
import picocli.CommandLine;

import java.io.PrintStream;

import static io.ballerina.cli.cmd.Constants.SHELL_COMMAND;

/**
 * This class represents the "ballerina shell" command.
 */
@CommandLine.Command(name = SHELL_COMMAND, description = "Launch ballerina shell")
public class ShellCommand implements BLauncherCmd {
    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    @CommandLine.Option(names = {"-d", "--debug"}, description = "Whether to enable debug mode from start.")
    private boolean isDebug = false;

    private final PrintStream errStream;

    public ShellCommand(PrintStream errStream) {
        this.errStream = errStream;
    }

    @Override
    public void execute() {
        try {
            Configuration configuration = new Configuration(isDebug, Configuration.EvaluatorMode.DEFAULT);
            ReplShellApplication.execute(configuration);
        } catch (Exception e) {
            errStream.println("Something went wrong: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return SHELL_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("launch the ballerina REPL shell");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  ballerina shell [-dhV] [-m=<mode>]\n");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
