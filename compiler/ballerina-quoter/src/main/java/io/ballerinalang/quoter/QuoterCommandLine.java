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
package io.ballerinalang.quoter;

import io.ballerinalang.quoter.config.QuoterCmdConfig;
import io.ballerinalang.quoter.config.QuoterConfig;
import org.apache.commons.cli.*;

public class QuoterCommandLine {
    public static void main(String[] args) {
        Options options = QuoterCmdConfig.getCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            QuoterConfig config = new QuoterCmdConfig(cmd);
            BallerinaQuoter.run(config);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("./gradlew quoter -Props=\"[OPTIONS]\"", options);
            System.exit(1);
        }
    }
}

