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
package io.ballerinalang.quoter.formatter;

import io.ballerinalang.quoter.QuoterConfig;
import io.ballerinalang.quoter.QuoterException;
import io.ballerinalang.quoter.segment.Segment;

import static io.ballerinalang.quoter.QuoterConfig.EXTERNAL_FORMATTER_NAME;

public abstract class SegmentFormatter {
    public abstract String format(Segment segment);

    /**
     * Creates a formatter based on the configuration option.
     */
    public static SegmentFormatter getFormatter(QuoterConfig config) {
        String formatterName = config.getOrThrow(EXTERNAL_FORMATTER_NAME);
        switch (formatterName) {
            case "none":
                return new NoFormatter();
            case "default":
                return new DefaultFormatter();
            case "class":
                return TemplateFormatter.fromConfig(config);
            default:
                throw new QuoterException("Unknown formatter name.");
        }
    }
}
