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
package io.ballerinalang.quoter.segment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Node factory API call generation segment.
 * Number of parameters may be zero or more.
 * Method call of format: "NodeFactory.<Type>createNodeType(param1, param2)"
 */
public class NodeFactorySegment extends Segment implements Iterable<Segment> {
    private final String methodName;
    protected final String genericType;
    private final List<Segment> parameters;

    public NodeFactorySegment(String methodName, String genericType) {
        this.methodName = methodName;
        this.parameters = new ArrayList<>();
        this.genericType = genericType;
    }

    public NodeFactorySegment(String methodName) {
        this.methodName = methodName;
        this.parameters = new ArrayList<>();
        this.genericType = null;
    }

    /**
     * Adds a new parameter to the method call.
     *
     * @param parameter Parameter segment.
     */
    public void addParameter(Segment parameter) {
        parameters.add(parameter);
    }

    @Override
    public StringBuilder stringBuilder() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("NodeFactory.")
                .append(genericType != null ? "<" + genericType + ">" : "")
                .append(getMethodName()).append("(");

        // Create comma separated parameter list.
        for (int i = 0; i < parameters.size(); i++) {
            if (i != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(parameters.get(i).stringBuilder());
        }
        stringBuilder.append(")");

        return stringBuilder;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Segment> iterator() {
        return Objects.requireNonNull(parameters.iterator());
    }

    /**
     * @return Name of the method of this factory call.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return Type of the method call. Found via stripping the create part from method name.
     */
    public String getType() {
        return methodName.substring(6);
    }

    /**
     * @return Generic type of the method call. null if doesn't have a generic type.
     */
    public String getGenericType() {
        return genericType;
    }
}
