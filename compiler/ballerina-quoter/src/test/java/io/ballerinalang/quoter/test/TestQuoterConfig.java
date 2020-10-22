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
package io.ballerinalang.quoter.test;

import io.ballerinalang.quoter.config.QuoterConfig;

import static io.ballerinalang.quoter.config.QuoterPropertiesConfig.*;

public class TestQuoterConfig extends QuoterConfig {
    private final String internalFormatterTemplate;
    private final String internalFormatterTemplateTabStart;
    private final String externalFormatterName;

    public TestQuoterConfig(String internalFormatterTemplate,
                            int internalFormatterTemplateTabStart,
                            String externalFormatterName) {
        this.internalFormatterTemplate = internalFormatterTemplate;
        this.internalFormatterTemplateTabStart = String.valueOf(internalFormatterTemplateTabStart);
        this.externalFormatterName = externalFormatterName;
    }

    @Override
    public String getOrThrow(String key) {
        switch (key) {
            case INTERNAL_FORMATTER_TEMPLATE:
                return internalFormatterTemplate;
            case INTERNAL_FORMATTER_TEMPLATE_TAB_START:
                return internalFormatterTemplateTabStart;
            case INTERNAL_NODE_CHILDREN_JSON:
                return "parameter-names.json";
            case EXTERNAL_FORMATTER_NAME:
                return externalFormatterName;
            default:
                throw new RuntimeException("Unknown key: " + key);
        }
    }
}
