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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.quoter.segment.NodeFactorySegment;
import io.ballerinalang.quoter.segment.generators.SegmentGenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Method reference object to cache the parameter types and generic types.
 */
public class MethodReference {
    private final Method method;
    private final Type[] parameterTypes;
    private final Type[] parameterGenericTypes;
    private final int offset;

    MethodReference(Method method) {
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
        this.parameterGenericTypes = method.getGenericParameterTypes();
        offset = requiresSyntaxKind() ? 1 : 0;
    }

    public String getName() {
        return method.getName();
    }

    public Method getMethod() {
        return method;
    }

    public Type getParameterType(int parameterIndex) {
        return parameterTypes[parameterIndex + offset];
    }

    public String getParameterGeneric(int parameterIndex) {
        String fullParameter = parameterGenericTypes[parameterIndex + offset].getTypeName();
        int lastDot = fullParameter.lastIndexOf('.');
        return fullParameter.substring(lastDot + 1, fullParameter.length() - 1);
    }

    public NodeFactorySegment toSegment() {
        return SegmentGenerator.createFactoryCallSegment(getName());
    }

    /**
     * Finds the offset (If first param is kind, there is a offset, otherwise no)
     */
    public boolean requiresSyntaxKind() {
        return parameterTypes[0] == SyntaxKind.class;
    }
}