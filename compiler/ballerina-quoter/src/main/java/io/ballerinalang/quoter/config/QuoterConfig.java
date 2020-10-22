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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerinalang.quoter.BallerinaQuoter;
import io.ballerinalang.quoter.QuoterException;
import io.ballerinalang.quoter.utils.FileReaderUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public abstract class QuoterConfig {
    public static final String INTERNAL_NODE_CHILDREN_JSON = "internal.node.children";
    public static final String EXTERNAL_FORMATTER_TEMPLATE = "external.formatter.template";
    public static final String EXTERNAL_FORMATTER_TEMPLATE_TAB_START = "external.formatter.template.tab.start";
    public static final String EXTERNAL_INPUT_FILE = "external.input.file";
    public static final String EXTERNAL_OUTPUT_FILE = "external.output.file";
    public static final String EXTERNAL_OUTPUT_SYS_OUT = "external.output.sys.out";
    public static final String EXTERNAL_FORMATTER_NAME = "external.formatter.name";

    abstract public String getOrThrow(String key);

    public boolean getBooleanOrThrow(String key) {
        return getOrThrow(key).equalsIgnoreCase("true");
    }

    /**
     * Read the template file specified in the configuration.
     */
    public String readTemplateFile() {
        return FileReaderUtils.readFile(getOrThrow(EXTERNAL_FORMATTER_TEMPLATE));
    }

    /**
     * Read input from the file specified in the configurations.
     */
    public String readInputFile() {
        String inputFileName = getOrThrow(EXTERNAL_INPUT_FILE);
        return FileReaderUtils.readFile(inputFileName);
    }

    /**
     * Output the content in the way specified in the configurations.
     */
    public void writeToOutputFile(String content) {
        String outputFileName = getOrThrow(EXTERNAL_OUTPUT_FILE);
        try (OutputStream outputStream = new FileOutputStream(outputFileName)) {
            outputStream.write(content.getBytes());
        } catch (IOException e) {
            throw new QuoterException("Failed to write " + outputFileName + ". Error: " + e.getMessage(), e);
        }

        if (getBooleanOrThrow(EXTERNAL_OUTPUT_SYS_OUT)) {
            System.out.println(content);
        }
    }

    /**
     * Get the node children config json specified in the configurations.
     */
    public Map<String, List<String>> readNodeChildrenJson() {
        String jsonFile = getOrThrow(INTERNAL_NODE_CHILDREN_JSON);
        ClassLoader classLoader = BallerinaQuoter.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(jsonFile)) {
            if (inputStream == null) throw new QuoterException("File not found: " + jsonFile);

            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            Type SIGNATURE_FORMAT = new TypeToken<Map<String, List<String>>>() {
            }.getType();
            return gson.fromJson(reader, SIGNATURE_FORMAT);
        } catch (IOException e) {
            throw new QuoterException("Failed to read " + jsonFile + ". Error: " + e.getMessage(), e);
        }
    }
}
