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

public class SegmentGeneratorDeclarationsTest extends AbstractQuoterTest {

    @Test
    public void testAnnotationDecl() {
        testAssertionFiles("declarations/annot-decl", "annot_decl_source_01");
        testAssertionFiles("declarations/annot-decl", "annot_decl_source_02");
        testAssertionFiles("declarations/annot-decl", "annot_decl_source_03");
    }

    @Test
    public void testClassDef() {
        testAssertionFiles("declarations/class-def", "class_def_source_01");
        testAssertionFiles("declarations/class-def", "class_def_source_02");
        testAssertionFiles("declarations/class-def", "class_def_source_03");
        testAssertionFiles("declarations/class-def", "class_def_source_04");
        testAssertionFiles("declarations/class-def", "class_def_source_11");
        testAssertionFiles("declarations/class-def", "class_def_source_12");
        testAssertionFiles("declarations/class-def", "class_def_source_13");
        testAssertionFiles("declarations/class-def", "class_def_source_14");
        testAssertionFiles("declarations/class-def", "class_def_source_24");
        testAssertionFiles("declarations/class-def", "class_def_source_25");
        testAssertionFiles("declarations/class-def", "class_def_source_27");
        testAssertionFiles("declarations/class-def", "class_def_source_28");
        testAssertionFiles("declarations/class-def", "class_def_source_36");
        testAssertionFiles("declarations/class-def", "class_def_source_38");
        testAssertionFiles("declarations/class-def", "class_def_source_40");
        testAssertionFiles("declarations/class-def", "class_def_source_41");
    }

    @Test
    public void testEnumDecl() {
        // TODO: Fails because enum declaration bug
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_01");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_02");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_03");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_04");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_05");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_06");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_07");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_08");
        testAssertionFiles("declarations/enum-decl", "enum_decl_source_09");
    }

    @Test
    public void testFuncDefinition() {
        testAssertionFiles("declarations/func-definition", "func_def_source_01");
        testAssertionFiles("declarations/func-definition", "func_def_source_02");
        testAssertionFiles("declarations/func-definition", "func_def_source_05");
        testAssertionFiles("declarations/func-definition", "func_def_source_08");
        testAssertionFiles("declarations/func-definition", "func_def_source_11");
        testAssertionFiles("declarations/func-definition", "func_def_source_14");
        testAssertionFiles("declarations/func-definition", "func_def_source_26");
        testAssertionFiles("declarations/func-definition", "func_params_source_01");
        testAssertionFiles("declarations/func-definition", "func_params_source_05");
        testAssertionFiles("declarations/func-definition", "func_params_source_08");
        testAssertionFiles("declarations/func-definition", "isolated_func_def_01");
        testAssertionFiles("declarations/func-definition", "isolated_func_def_02");
        testAssertionFiles("declarations/func-definition", "isolated_func_def_03");
        testAssertionFiles("declarations/func-definition", "isolated_func_def_04");
        testAssertionFiles("declarations/func-definition", "isolated_func_def_05");
        testAssertionFiles("declarations/func-definition", "isolated_func_def_06");
    }

    @Test
    public void testImportDecl() {
        testAssertionFiles("declarations/import-decl", "import_decl_source_01");
        testAssertionFiles("declarations/import-decl", "import_decl_source_02");
        testAssertionFiles("declarations/import-decl", "import_decl_source_03");
        testAssertionFiles("declarations/import-decl", "import_decl_source_04");
        testAssertionFiles("declarations/import-decl", "import_decl_source_05");
        testAssertionFiles("declarations/import-decl", "import_decl_source_06");
        testAssertionFiles("declarations/import-decl", "import_decl_source_07");
        testAssertionFiles("declarations/import-decl", "import_decl_source_08");
        testAssertionFiles("declarations/import-decl", "import_decl_source_09");
        testAssertionFiles("declarations/import-decl", "import_decl_source_18");
    }

    @Test
    public void testIsolatedObjectMethods() {
        testAssertionFiles("declarations/isolated-object-methods", "isolated_object_method_source_01");
        testAssertionFiles("declarations/isolated-object-methods", "isolated_object_method_source_03");
    }

    @Test
    public void testIsolatedServiceFunctions() {
        // TODO: Fails because service name is null
        testAssertionFiles("declarations/isolated-service-functions", "isolated_service_func_source_01");
        testAssertionFiles("declarations/isolated-service-functions", "isolated_service_func_source_02");
        testAssertionFiles("declarations/isolated-service-functions", "isolated_service_func_source_03");
        testAssertionFiles("declarations/isolated-service-functions", "isolated_service_func_source_04");
    }

    @Test
    public void testModuleVarDecl() {
        // TODO: equalsToken must not be null?
        testAssertionFiles("declarations/module-var-decl", "module_var_decl_source_01");
        testAssertionFiles("declarations/module-var-decl", "module_var_decl_source_04");
        // TODO: Incompatible types: no instance(s) of type variable(s) T exist so
        // TODO: that io.ballerina.compiler.syntax.tree.NodeList<T> conforms to io.ballerina.compiler.syntax.tree.MetadataNode
        testAssertionFiles("declarations/module-var-decl", "module_var_decl_source_10");
    }

    @Test
    public void testObjectTypeDef() {
        // TODO: equalsToken must not be null?
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_01");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_02");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_03");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_04");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_11");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_12");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_13");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_14");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_16");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_24");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_36");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_38");
        testAssertionFiles("declarations/object-type-def", "object_type_def_source_41");
    }

    @Test
    public void testRecordTypeDef() {
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_01");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_02");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_03");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_04");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_08");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_11");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_13");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_14");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_15");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_19");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_21");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_22");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_23");
        testAssertionFiles("declarations/record-type-def", "record_type_def_source_25");
    }

    @Test
    public void testServiceDecl() {
        // TODO: Fails because service name is null
        testAssertionFiles("declarations/service-decl", "service_decl_source_01");
        testAssertionFiles("declarations/service-decl", "service_decl_source_02");
        testAssertionFiles("declarations/service-decl", "service_decl_source_03");
        testAssertionFiles("declarations/service-decl", "service_decl_source_04");
        testAssertionFiles("declarations/service-decl", "service_decl_source_05");
        testAssertionFiles("declarations/service-decl", "service_decl_source_06");
        testAssertionFiles("declarations/service-decl", "service_decl_source_07");
        testAssertionFiles("declarations/service-decl", "service_decl_source_08");
        testAssertionFiles("declarations/service-decl", "service_decl_source_09");
        testAssertionFiles("declarations/service-decl", "service_decl_source_10");
        testAssertionFiles("declarations/service-decl", "service_decl_source_11");
        testAssertionFiles("declarations/service-decl", "service_decl_source_12");
        testAssertionFiles("declarations/service-decl", "service_decl_source_13");
        testAssertionFiles("declarations/service-decl", "service_decl_source_14");
        testAssertionFiles("declarations/service-decl", "service_decl_source_15");
    }

    @Test
    public void testTransactionalResource() {
        // TODO: Fails because service name is null
        testAssertionFiles("declarations/transactional-resource", "transaction_resource_func_source_01");
    }

    @Test
    public void testXmlnsDecl() {
        testAssertionFiles("declarations/xmlns-decl", "xmlns_decl_source_01");
        testAssertionFiles("declarations/xmlns-decl", "xmlns_decl_source_06");
    }
}
