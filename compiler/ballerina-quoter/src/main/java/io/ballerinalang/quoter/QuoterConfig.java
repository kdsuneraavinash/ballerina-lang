/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.quoter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton that represent a set of config properties loaded from a config file.
 */
public class QuoterConfig {
    public static final String INTERNAL_FORMATTER_TEMPLATE = "internal.formatter.template";
    public static final String INTERNAL_FORMATTER_TEMPLATE_TAB_START = "internal.formatter.template.tab.start";
    public static final String EXTERNAL_NODE_CHILDREN_JSON = "external.node.children";
    public static final String EXTERNAL_INPUT_FILE = "external.input.file";
    public static final String EXTERNAL_OUTPUT_FILE = "external.output.file";
    public static final String EXTERNAL_OUTPUT_SYS_OUT = "external.output.sys.out";
    public static final String EXTERNAL_FORMATTER_NAME = "external.formatter.name";
    private static final String QUOTER_GEN_CONFIG_PROPERTIES = "quoter-config.properties";
    private static QuoterConfig instance;

    private final Properties props;

    static QuoterConfig getInstance() {
        if (instance == null) instance = new QuoterConfig(loadConfig());
        return instance;
    }

    private QuoterConfig(Properties props) {
        this.props = props;
    }

    /**
     * Load the property of the given key.
     * Throws if not found.
     */
    public String getOrThrow(String key) {
        if (Objects.isNull(key)) throw new QuoterException("The QuoterConfig key must not be null");
        String value = this.props.getProperty(key);
        if (Objects.isNull(value)) throw new QuoterException("The value of QuoterConfig key '" + key + "' is null");
        return value;
    }

    public boolean getBooleanOrThrow(String key) {
        return getOrThrow(key).equalsIgnoreCase("true");
    }

    /**
     * Loads the properties from the resources.
     */
    private static Properties loadConfig() {
        String path = QUOTER_GEN_CONFIG_PROPERTIES;
        ClassLoader classLoader = BallerinaQuoter.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) throw new QuoterException("File not found: " + path);
            Properties props = new Properties();
            props.load(inputStream);
            return props;
        } catch (IOException e) {
            throw new QuoterException("Project properties loading failed. Reason: " + e, e);
        }
    }
}
