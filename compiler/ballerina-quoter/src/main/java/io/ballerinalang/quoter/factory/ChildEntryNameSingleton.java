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
package io.ballerinalang.quoter.factory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerinalang.quoter.BallerinaQuoter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Helper class to load the method param name map.
 * <p>
 * Required because NonTerminalNode does not expose all of its child names.
 * Loads the method->[paramNames] as a singleton.
 */
public class ChildEntryNameSingleton {
    private static ChildEntryNameSingleton instance;
    private final Map<String, List<String>> json;

    private ChildEntryNameSingleton(Map<String, List<String>> json) {
        this.json = json;
    }

    /**
     * Get the instance. If not initialized yet, initialize the object.
     */
    static ChildEntryNameSingleton getInstance() {
        if (instance == null) {
            try {
                Gson gson = new Gson();
                InputStream inputStream = BallerinaQuoter.class.getResourceAsStream("/quoter/signatures.json");
                if (inputStream == null) throw new IOException("Signature file not found.");

                Type SIGNATURE_FORMAT = new TypeToken<Map<String, List<String>>>() {
                }.getType();
                InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

                instance = new ChildEntryNameSingleton(gson.fromJson(reader, SIGNATURE_FORMAT));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Signature file reading failed: " + e.getMessage());
            }
        }
        return instance;
    }

    /**
     * Get the child entry names of the given method.
     */
    List<String> childEntryNames(String method) {
        return json.get(method);
    }
}