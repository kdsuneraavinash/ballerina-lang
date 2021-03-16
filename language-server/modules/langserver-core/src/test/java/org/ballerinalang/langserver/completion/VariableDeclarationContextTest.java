/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completion;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Tests for {@link org.ballerinalang.langserver.completions.providers.context.VariableDeclarationProvider}
 * completion provider.
 */
public class VariableDeclarationContextTest extends CompletionTestNew {

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"var_def_ctx_config1.json", getTestResourceDir()},
                {"var_def_ctx_config2.json", getTestResourceDir()},
                {"var_def_ctx_config3.json", getTestResourceDir()},
                {"var_def_ctx_config4.json", getTestResourceDir()},
                {"var_def_ctx_config5.json", getTestResourceDir()},
                {"var_def_ctx_config6.json", getTestResourceDir()},
                {"project_var_def_ctx_config1.json", getTestResourceDir()},
                {"project_var_def_ctx_config2.json", getTestResourceDir()},
                {"project_var_def_ctx_config3.json", getTestResourceDir()}
        };
    }

    @Override
    public String getTestResourceDir() {
        return "variable-declaration";
    }
}
