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
package io.ballerinalang.quoter.segment.generators.cache;

import io.ballerinalang.quoter.config.QuoterConfig;

import java.util.*;


/**
 * Helper class to load the method param name map.
 * <p>
 * Required because NonTerminalNode does not expose all of its child names.
 * Load each method call with its required parameter names.
 */
public class ParameterNameCache {
    private final Map<String, List<String>> cache;

    private ParameterNameCache(Map<String, List<String>> cache) {
        this.cache = cache;
    }

    /**
     * Create the cache from the file defined in the config.
     */
    public static ParameterNameCache fromConfig(QuoterConfig config) {
        return new ParameterNameCache(config.readNodeChildrenJson());
    }

    /**
     * Get the parameter entry names of the given method.
     */
    public List<String> getParameterNames(String method) {
        return cache.get(method);
    }
}