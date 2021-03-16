/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.observability.tracing;

import org.ballerina.testobserve.tracing.extension.BMockSpan;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test cases for remove function calls.
 */
@Test(groups = "tracing-test")
public class RemoteCallTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "03_remote_call.bal";
    private static final String SERVICE_NAME = "testServiceThree";
    private static final String BASE_URL = "http://localhost:9093";

    @Test
    public void testNestedRemoteCalls() throws Exception {
        final String resourceName = "resourceOne";
        final String resourceFunctionPosition = FILE_NAME + ":22:5";
        final String span2Position = FILE_NAME + ":23:9";
        final String span3Position = MOCK_CLIENT_FILE_NAME + ":32:9";
        final String span4Position = FILE_NAME + ":24:20";

        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, DEFAULT_MODULE_ID, resourceFunctionPosition);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, span2Position, span3Position, span4Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("src.object.name", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(),
                    MOCK_CLIENT_OBJECT_NAME + ":callAnotherRemoteFunction");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callAnotherRemoteFunction")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span2.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithNoReturn");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", UTILS_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithNoReturn")
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span4Position))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span4Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }

    @DataProvider(name = "remote-error-return-data-provider")
    public Object[][] getRemoteErrorReturnData() {
        return new Object[][]{
                {"resourceTwo", FILE_NAME + ":28:5", FILE_NAME + ":29:15", "Test Error\n" +
                        "    at intg_tests.tracing_tests.utils.0_0_1.MockClient:callWithErrorReturn" +
                        "(mock_client_endpoint.bal:46)\n" +
                        "       intg_tests.tracing_tests.0_0_1.$anonType$_0:$post$resourceTwo(03_remote_call.bal:29)"},
                {"resourceThree", FILE_NAME + ":34:5", FILE_NAME + ":35:20", "Test Error\n" +
                        "    at intg_tests.tracing_tests.utils.0_0_1.MockClient:callWithErrorReturn" +
                        "(mock_client_endpoint.bal:46)\n" +
                        "       intg_tests.tracing_tests.0_0_1.$anonType$_0:$post$resourceThree(03_remote_call.bal:35)"}
        };
    }

    @Test(dataProvider = "remote-error-return-data-provider")
    public void testRemoteFunctionErrorReturn(String resourceName, String resourceFunctionPosition,
                                              String remoteCallPosition, String errorMessage) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 500);
        Assert.assertEquals(httpResponse.getData(), "Test Error");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, DEFAULT_MODULE_ID, resourceFunctionPosition);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, remoteCallPosition)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Map<String, Object> tags = span.getTags();
//            TODO: Remove the bellow line once #ballerina-lang/issues/28686 is fixed
            tags.keySet().removeIf(key -> key.equals("error.message"));
            Assert.assertEquals(tags, toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("src.object.name", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("error", "true"),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
//                    TODO: Uncomment the bellow line once #ballerina-lang/issues/28686 is fixed
//                    new AbstractMap.SimpleEntry<>("error.message", errorMessage),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), remoteCallPosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithErrorReturn");
            Map<String, Object> tags = span.getTags();
//            TODO: Remove the bellow line once #ballerina-lang/issues/28686 is fixed
            tags.keySet().removeIf(key -> key.equals("error.message"));
            Assert.assertEquals(tags, toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", remoteCallPosition),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithErrorReturn"),
//                    TODO: Uncomment the bellow line once #ballerina-lang/issues/28686 is fixed
//                    new AbstractMap.SimpleEntry<>("error.message", errorMessage),
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });
    }

    @Test
    public void testIgnoredErrorReturnInRemoteCall() throws Exception {
        final String resourceName = "resourceFour";
        final String resourceFunctionPosition = FILE_NAME + ":40:5";
        final String span2Position = FILE_NAME + ":41:19";
        final String span3Position = FILE_NAME + ":42:20";

        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, DEFAULT_MODULE_ID, resourceFunctionPosition);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, span2Position, span3Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("src.object.name", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithErrorReturn");
            Map<String, Object> tags = span.getTags();
//            TODO: Remove the bellow line once #ballerina-lang/issues/28686 is fixed
            tags.keySet().removeIf(key -> key.equals("error.message"));
            Assert.assertEquals(tags, toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithErrorReturn"),
//                    TODO: Uncomment the bellow line once #ballerina-lang/issues/28686 is fixed
//                    new AbstractMap.SimpleEntry<>("error.message", "Test Error\n" +
//                            "    at intg_tests.tracing_tests.utils.0_0_1.MockClient:callWithErrorReturn" +
//                            "(mock_client_endpoint.bal:46)\n" +
//                            "       intg_tests.tracing_tests.0_0_1.$anonType$_8:$post$resourceFour" +
//                            "(03_remote_call.bal:41)")
                    new AbstractMap.SimpleEntry<>("error", "true")

            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }

    @Test
    public void testTrappedPanic() throws Exception {
        final String resourceName = "resourceFive";
        final String resourceFunctionPosition = FILE_NAME + ":46:5";
        final String span2Position = FILE_NAME + ":47:24";
        final String span3Position = FILE_NAME + ":49:24";

        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Successfully trapped panic: Test Error");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, DEFAULT_MODULE_ID, resourceFunctionPosition);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, span2Position, span3Position)));
        Assert.assertEquals(spans.stream().filter(bMockSpan -> bMockSpan.getParentId() == 0).count(), 1);

        Optional<BMockSpan> span1 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), resourceFunctionPosition))
                .findFirst();
        Assert.assertTrue(span1.isPresent());
        long traceId = span1.get().getTraceId();
        span1.ifPresent(span -> {
            Assert.assertTrue(spans.stream().noneMatch(mockSpan -> mockSpan.getTraceId() == traceId
                    && mockSpan.getSpanId() == span.getParentId()));
            Assert.assertEquals(span.getOperationName(), "post /" + resourceName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "server"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.service.resource", "true"),
                    new AbstractMap.SimpleEntry<>("http.url", "/" + SERVICE_NAME + "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("http.method", "POST"),
                    new AbstractMap.SimpleEntry<>("protocol", "http"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", SERVICE_NAME),
                    new AbstractMap.SimpleEntry<>("listener.name", SERVER_CONNECTOR_NAME),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName),
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post")
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span2Position))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), MOCK_CLIENT_OBJECT_NAME + ":callWithPanic");
            Map<String, Object> tags = span.getTags();
//            TODO: Remove the bellow line once #ballerina-lang/issues/28686 is fixed
            tags.keySet().removeIf(key -> key.equals("error.message"));
            Assert.assertEquals(tags, toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span2Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", MOCK_CLIENT_OBJECT_NAME),
                    new AbstractMap.SimpleEntry<>("src.function.name", "callWithPanic"),
//                    TODO: Uncomment the bellow line once #ballerina-lang/issues/28686 is fixed
//                    new AbstractMap.SimpleEntry<>("error.message", "Test Error\n" +
//                            "    at intg_tests.tracing_tests.utils.0_0_1.MockClient:callWithPanic" +
//                            "(mock_client_endpoint.bal:58)\n" +
//                            "       intg_tests.tracing_tests.0_0_1.$anonType$_7:$post$resourceFive" +
//                            "(03_remote_call.bal:47)")
                    new AbstractMap.SimpleEntry<>("error", "true")
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), span3Position))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", span3Position),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }
}
