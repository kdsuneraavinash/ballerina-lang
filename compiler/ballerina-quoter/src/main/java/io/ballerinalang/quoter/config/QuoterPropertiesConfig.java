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

import io.ballerinalang.quoter.BallerinaQuoter;
import io.ballerinalang.quoter.QuoterException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Singleton that represent a set of config properties loaded from a config file.
 */
public class QuoterPropertiesConfig extends QuoterConfig {
    protected static final String QUOTER_GEN_CONFIG_PROPERTIES = "quoter-config.properties";

    protected final Properties props;

    public QuoterPropertiesConfig() {
        this.props = loadConfig();
    }

    /**
     * Load the property of the given key.
     * Throws if not found.
     */
    @Override
    public String getOrThrow(String key) {
        if (Objects.isNull(key)) throw new QuoterException("The QuoterConfig key must not be null");
        String value = this.props.getProperty(key);
        if (Objects.isNull(value)) throw new QuoterException("The value of QuoterConfig key '" + key + "' is null");
        return value;
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
