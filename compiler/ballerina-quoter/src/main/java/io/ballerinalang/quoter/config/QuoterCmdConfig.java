/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerinalang.quoter.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Objects;

/**
 * Configuration file for CLI application.
 */
public class QuoterCmdConfig extends QuoterPropertiesConfig {
    private final String formatterUseTemplate;
    private final String formatterTemplate;
    private final String formatterTabStart;
    private final String nodeChildren;
    private final String inputFile;
    private final String outputFile;
    private final String outputSysOut;
    private final String formatterName;

    public QuoterCmdConfig(CommandLine cmd) {
        this.formatterUseTemplate = cmd.getOptionValue('u');
        this.formatterTemplate = cmd.getOptionValue('t');
        this.formatterTabStart = cmd.getOptionValue('p');
        this.nodeChildren = null;
        this.inputFile = cmd.getOptionValue('i');
        this.outputFile = cmd.getOptionValue('o');
        this.outputSysOut = cmd.getOptionValue('s');
        this.formatterName = cmd.getOptionValue('f');
    }

    /**
     * Generate the CLI options.
     *
     * @return Generated CLI options.
     */
    public static Options getCommandLineOptions() {
        Options options = new Options();
        addArgument(options, "input", "input file path");
        addArgument(options, "output", "output file path");
        addArgument(options, "stdout", "output to stdout (true/false)");
        addArgument(options, "formatter", "formatter name (none,default,template,variable)");
        addArgument(options, "use template", "whether to use template (true/false)");
        addArgument(options, "template", "template to use (applicable only in template formatter)");
        addArgument(options, "position", "tab position to start (applicable only in template formatter)");
        return options;
    }

    /**
     * Add a new argument to a given {@link Options} object.
     *
     * @param options     Options obj
     * @param name        Name of the argument
     * @param description Description of argument.
     */
    private static void addArgument(Options options, String name, String description) {
        Option option = new Option(name.substring(0, 1), name, true, description);
        option.setRequired(false);
        options.addOption(option);
    }

    @Override
    public String getOrThrow(String key) {
        switch (key) {
            case EXTERNAL_FORMATTER_USE_TEMPLATE:
                return overrideGet(key, formatterUseTemplate);
            case EXTERNAL_FORMATTER_TEMPLATE:
                return overrideGet(key, formatterTemplate);
            case EXTERNAL_FORMATTER_TAB_START:
                return overrideGet(key, formatterTabStart);
            case INTERNAL_NODE_CHILDREN_JSON:
                return overrideGet(key, nodeChildren);
            case EXTERNAL_INPUT_FILE:
                return overrideGet(key, inputFile);
            case EXTERNAL_OUTPUT_FILE:
                return overrideGet(key, outputFile);
            case EXTERNAL_OUTPUT_SYS_OUT:
                return overrideGet(key, outputSysOut);
            case EXTERNAL_FORMATTER_NAME:
                return overrideGet(key, formatterName);
            default:
                return super.getOrThrow(key);
        }
    }

    /**
     * Get the value assigned to a key from either config file
     * or a overridden value.
     * If overridden value is null, reads from the config file instead.
     *
     * @param key      Property key
     * @param override Value to override the config file value with
     * @return Value assigned to key
     */
    private String overrideGet(String key, String override) {
        if (Objects.isNull(override)) {
            return super.getOrThrow(key);
        }
        return override;
    }
}
