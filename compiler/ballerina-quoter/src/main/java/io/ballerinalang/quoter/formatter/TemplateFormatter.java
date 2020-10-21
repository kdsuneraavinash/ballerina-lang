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

import io.ballerinalang.quoter.BallerinaQuoter;
import io.ballerinalang.quoter.QuoterConfig;
import io.ballerinalang.quoter.QuoterException;
import io.ballerinalang.quoter.segment.Segment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static io.ballerinalang.quoter.QuoterConfig.INTERNAL_FORMATTER_TEMPLATE;
import static io.ballerinalang.quoter.QuoterConfig.INTERNAL_FORMATTER_TEMPLATE_TAB_START;

/**
 * Formatter that inserts the default formatter output in a template.
 * Template is defined via configs.
 */
public class TemplateFormatter extends DefaultFormatter {
    private final String baseTemplate;
    private final int tabStart;

    public TemplateFormatter(String baseTemplate, int tabStart) {
        this.baseTemplate = baseTemplate;
        this.tabStart = tabStart;
    }

    /**
     * Create the formatter via config.
     */
    public static TemplateFormatter fromConfig(QuoterConfig config) {
        String inputFileName = config.getOrThrow(INTERNAL_FORMATTER_TEMPLATE);
        int tabStart = Integer.parseInt(config.getOrThrow(INTERNAL_FORMATTER_TEMPLATE_TAB_START));
        ClassLoader classLoader = BallerinaQuoter.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(inputFileName)) {
            if (inputStream == null) {
                String errorMessage = "Class formatter template file not found: " + INTERNAL_FORMATTER_TEMPLATE;
                throw new QuoterException(errorMessage);
            }

            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String input = scanner.hasNext() ? scanner.next() : "";
            return new TemplateFormatter(input, tabStart);
        } catch (IOException e) {
            throw new QuoterException("Failed to read " + inputFileName + ". Error: " + e.getMessage(), e);
        }
    }

    @Override
    protected int getInitialDepth() {
        return tabStart;
    }

    @Override
    public String format(Segment segment) {
        String nodeString = super.format(segment);
        return String.format(baseTemplate, nodeString);
    }
}
