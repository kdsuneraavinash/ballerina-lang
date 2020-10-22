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

import io.ballerinalang.quoter.QuoterException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a cache of each method name to its reference to make later lookups faster.
 * This uses reflection to find the method.
 * If the method is not found throws a RuntimeException.
 */
public class MethodCache {
    private final Map<String, Method> cache;

    private MethodCache(Map<String, Method> cache) {
        this.cache = cache;
    }

    public static <T> MethodCache fromClassRef(Class<T> tClass) {
        Map<String, Method> methodCache = new HashMap<>();
        Method[] methodNames = tClass.getMethods();
        for (Method method : methodNames) {
            methodCache.put(method.getName(), method);
        }
        return new MethodCache(methodCache);
    }

    public Method getMethod(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        throw new QuoterException("Failed to find method " + name);
    }
}
