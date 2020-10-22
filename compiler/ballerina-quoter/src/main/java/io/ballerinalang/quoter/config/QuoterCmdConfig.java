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

import java.util.Objects;

import org.apache.commons.cli.*;


public class QuoterCmdConfig extends QuoterPropertiesConfig {
    private final String formatterTemplate;
    private final String formatterTemplateTabStart;
    private final String nodeChildren;
    private final String inputFile;
    private final String outputFile;
    private final String outputSysOut;
    private final String formatterName;

    public QuoterCmdConfig(CommandLine cmd) {
        this.formatterTemplate = cmd.getOptionValue('t');
        this.formatterTemplateTabStart = cmd.getOptionValue('p');
        this.nodeChildren = null;
        this.inputFile = cmd.getOptionValue('i');
        this.outputFile = cmd.getOptionValue('o');
        this.outputSysOut = cmd.getOptionValue('s');
        this.formatterName = cmd.getOptionValue('f');
    }

    public static Options getCommandLineOptions() {
        Options options = new Options();
        addArgument(options, "input", "input file path");
        addArgument(options, "output", "output file path");
        addArgument(options, "stdout", "output to stdout (true/false)");
        addArgument(options, "formatter", "formatter name (none,default,template,variable)");
        addArgument(options, "template", "template to use (applicable only in template formatter)");
        addArgument(options, "position", "tab position to start (applicable only in template formatter)");
        return options;
    }

    private static void addArgument(Options options, String name, String description) {
        Option option = new Option(name.substring(0, 1), name, true, description);
        option.setRequired(false);
        options.addOption(option);
    }

    @Override
    public String getOrThrow(String key) {
        switch (key) {
            case EXTERNAL_FORMATTER_TEMPLATE:
                return overrideGet(key, formatterTemplate);
            case EXTERNAL_FORMATTER_TEMPLATE_TAB_START:
                return overrideGet(key, formatterTemplateTabStart);
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

    private String overrideGet(String key, String override) {
        if (Objects.isNull(override)) return super.getOrThrow(key);
        return override;
    }
}
