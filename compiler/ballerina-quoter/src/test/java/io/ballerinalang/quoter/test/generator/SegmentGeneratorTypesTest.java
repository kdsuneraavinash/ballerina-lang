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
package io.ballerinalang.quoter.test.generator;

import io.ballerinalang.quoter.test.AbstractQuoterTest;
import org.testng.annotations.Test;

public class SegmentGeneratorTypesTest extends AbstractQuoterTest {
    @Test
    public void testArrayType() {
        testAssertionFiles("types/array-type", "array_type_assert_01");
        testAssertionFiles("types/array-type", "array_type_assert_02");
        testAssertionFiles("types/array-type", "array_type_assert_03");
    }

    @Test
    public void testErrorType() {
        testAssertionFiles("types/error-type", "error_type_assert_01");
        testAssertionFiles("types/error-type", "error_type_assert_02");
        testAssertionFiles("types/error-type", "error_type_assert_03");
    }

    @Test
    public void testFuncType() {
        testAssertionFiles("types/func-type", "func_type_source_01");
        testAssertionFiles("types/func-type", "func_type_source_02");
        testAssertionFiles("types/func-type", "func_type_source_03");
        testAssertionFiles("types/func-type", "func_type_source_04");
    }

    @Test
    public void testIntersectionType() {
        testAssertionFiles("types/intersection-type", "intersection_type_source_01");
        testAssertionFiles("types/intersection-type", "intersection_type_source_04");
        testAssertionFiles("types/intersection-type", "intersection_type_source_06");
    }

    @Test
    public void testOptionalType() {
        testAssertionFiles("types/optional-type", "optional_type_source_01");
        testAssertionFiles("types/optional-type", "optional_type_source_02");
        testAssertionFiles("types/optional-type", "optional_type_source_04");
    }

    @Test
    public void testParameterizedType() {
        testAssertionFiles("types/parameterized-type", "parameterized_type_source_01");
        testAssertionFiles("types/parameterized-type", "parameterized_type_source_11");
        testAssertionFiles("types/parameterized-type", "parameterized_type_source_14");
    }

    @Test
    public void testSimpleType() {
        testAssertionFiles("types/simple-types", "simple_types_source_01");
    }

    @Test
    public void testSingletonType() {
        testAssertionFiles("types/singleton-type", "singleton_type_source_01");
        testAssertionFiles("types/singleton-type", "singleton_type_source_02");
        testAssertionFiles("types/singleton-type", "singleton_type_source_03");
    }

    @Test
    public void testStreamType() {
        testAssertionFiles("types/stream-type", "stream_type_source_01");
        testAssertionFiles("types/stream-type", "stream_type_source_03");
        testAssertionFiles("types/stream-type", "stream_type_source_04");
    }

    @Test
    public void testTableType() {
        testAssertionFiles("types/table-type", "table_type_source_01");
        testAssertionFiles("types/table-type", "table_type_source_06");
        testAssertionFiles("types/table-type", "table_type_source_07");
    }

    @Test
    public void testTupleType() {
        testAssertionFiles("types/tuple-type", "tuple_type_source_01");
        testAssertionFiles("types/tuple-type", "tuple_type_source_02");
        testAssertionFiles("types/tuple-type", "tuple_type_source_03");
    }

    @Test
    public void testUnionType() {
        testAssertionFiles("types/union-type", "union_type_assert_01");
        testAssertionFiles("types/union-type", "union_type_assert_02");
    }
}
