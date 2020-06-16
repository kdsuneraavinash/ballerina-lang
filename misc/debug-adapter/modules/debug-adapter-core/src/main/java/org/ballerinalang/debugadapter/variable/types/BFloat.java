/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.variable.types;

import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.variable.BPrimitiveVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.eclipse.lsp4j.debug.Variable;

import java.util.stream.Collectors;

/**
 * Ballerina float variable type.
 */
public class BFloat extends BPrimitiveVariable {

    public BFloat(Value value, Variable dapVariable) {
        super(BVariableType.FLOAT, value, dapVariable);
    }

    @Override
    public String computeValue() {
        if (jvmValue instanceof DoubleValue) {
            return jvmValue.toString();
        } else if (jvmValue instanceof ObjectReference) {
            ObjectReference valueObjectRef = ((ObjectReference) jvmValue);
            Field valueField = valueObjectRef.referenceType().allFields().stream().filter(field ->
                    field.name().equals("value")).collect(Collectors.toList()).get(0);
            return valueObjectRef.getValue(valueField).toString();
        } else {
            return "unknown";
        }
    }
}
