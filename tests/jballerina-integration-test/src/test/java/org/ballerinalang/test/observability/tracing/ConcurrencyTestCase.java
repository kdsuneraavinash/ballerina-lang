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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test cases for concurrency related functionality.
 */
@Test(groups = "tracing-test")
public class ConcurrencyTestCase extends TracingBaseTestCase {
    private static final String FILE_NAME = "05_concurrency.bal";
    private static final String SERVICE_NAME = "testServiceFive";
    private static final String BASE_URL = "http://localhost:9095";

    @DataProvider(name = "async-call-data-provider")
    public Object[][] getAsyncCallData() {
        return new Object[][] {
                {"resourceOne", FILE_NAME + ":23:5", FILE_NAME + ":24:33", FILE_NAME + ":30:20",
                        MOCK_CLIENT_OBJECT_NAME, "calculateSum", null, null},
                {"resourceTwo", FILE_NAME + ":34:5", FILE_NAME + ":35:33", FILE_NAME + ":41:20",
                        null, null, null, "calculateSumWithObservability"},
                {"resourceThree", FILE_NAME + ":45:5", FILE_NAME + ":47:33", FILE_NAME + ":53:20",
                        null, null, OBSERVABLE_ADDER_OBJECT_NAME, "getSum"}
        };
    }

    @Test(dataProvider = "async-call-data-provider")
    public void testAsyncCall(String resourceName, String resourceFunctionPosition, String asyncCallPosition,
                              String callerRespondPosition, String asyncCallConnectorName, String asyncCallActionName,
                              String asyncCallObjectName, String asyncCallFunctionName) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, DEFAULT_MODULE_ID, resourceFunctionPosition);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, asyncCallPosition, callerRespondPosition)));
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
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), asyncCallPosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), asyncCallFunctionName == null
                    ? asyncCallConnectorName + ":" + asyncCallActionName
                    : (asyncCallObjectName == null ? "" : asyncCallObjectName + ":") + asyncCallFunctionName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", asyncCallPosition),
                    asyncCallActionName == null ? null : new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    asyncCallConnectorName == null
                            ? null
                            : new AbstractMap.SimpleEntry<>("src.object.name", asyncCallConnectorName),
                    asyncCallActionName == null
                            ? null
                            : new AbstractMap.SimpleEntry<>("src.function.name", asyncCallActionName),
                    asyncCallObjectName == null
                            ? null
                            : new AbstractMap.SimpleEntry<>("src.object.name", asyncCallObjectName),
                    asyncCallFunctionName == null
                            ? null
                            : new AbstractMap.SimpleEntry<>("src.function.name", asyncCallFunctionName)
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), callerRespondPosition))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", callerRespondPosition),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }

    @DataProvider(name = "workers-data-provider")
    public Object[][] getWorkersData() {
        final String w1Position = FILE_NAME + ":104:15";
        final String w2Position = FILE_NAME + ":111:15";
        return new Object[][] {
                {"resourceFour", FILE_NAME + ":57:5", "w1", w1Position, "w2", w2Position, FILE_NAME + ":59:20"},
                {"resourceFive", FILE_NAME + ":63:5", "w1", w1Position, "w2", w2Position, FILE_NAME + ":65:20"},
                {"resourceSix", FILE_NAME + ":69:5", "w1", w1Position, "w2", w2Position, FILE_NAME + ":71:20"},
                {"resourceSeven", FILE_NAME + ":75:5", "w3", FILE_NAME + ":78:35", "w4", FILE_NAME + ":86:35",
                        FILE_NAME + ":99:20"}
        };
    }

    @Test(dataProvider = "workers-data-provider")
    public void testWorkers(String resourceName, String resourceFunctionPosition,
                                       String workerAName, String workerAPosition, String workerBName,
                                       String workerBPosition, String callerRespondPosition) throws Exception {
        HttpResponse httpResponse = HttpClientRequest.doPost(BASE_URL + "/" + SERVICE_NAME + "/" + resourceName,
                "", Collections.emptyMap());
        Assert.assertEquals(httpResponse.getResponseCode(), 200);
        Assert.assertEquals(httpResponse.getData(), "Invocation Successful");
        Thread.sleep(1000);

        List<BMockSpan> spans = this.getFinishedSpans(SERVICE_NAME, DEFAULT_MODULE_ID, resourceFunctionPosition);
        Assert.assertEquals(spans.stream()
                        .map(span -> span.getTags().get("src.position"))
                        .collect(Collectors.toSet()),
                new HashSet<>(Arrays.asList(resourceFunctionPosition, workerAPosition, workerBPosition,
                        callerRespondPosition)));
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
                    new AbstractMap.SimpleEntry<>("src.resource.accessor", "post"),
                    new AbstractMap.SimpleEntry<>("src.resource.path", "/" + resourceName)
            ));
        });

        Optional<BMockSpan> span2 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), workerAPosition))
                .findFirst();
        Assert.assertTrue(span2.isPresent());
        span2.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), workerAName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", workerAPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.function.name", workerAName)
            ));
        });

        Optional<BMockSpan> span3 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), workerBPosition))
                .findFirst();
        Assert.assertTrue(span3.isPresent());
        span3.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), workerBName);
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", workerBPosition),
                    new AbstractMap.SimpleEntry<>("src.worker", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.function.name", workerBName)
            ));
        });

        Optional<BMockSpan> span4 = spans.stream()
                .filter(bMockSpan -> Objects.equals(bMockSpan.getTags().get("src.position"), callerRespondPosition))
                .findFirst();
        Assert.assertTrue(span4.isPresent());
        span4.ifPresent(span -> {
            Assert.assertEquals(span.getTraceId(), traceId);
            Assert.assertEquals(span.getParentId(), span1.get().getSpanId());
            Assert.assertEquals(span.getOperationName(), "ballerina/testobserve/Caller:respond");
            Assert.assertEquals(span.getTags(), toMap(
                    new AbstractMap.SimpleEntry<>("span.kind", "client"),
                    new AbstractMap.SimpleEntry<>("src.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("src.position", callerRespondPosition),
                    new AbstractMap.SimpleEntry<>("src.client.remote", "true"),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.module", DEFAULT_MODULE_ID),
                    new AbstractMap.SimpleEntry<>("entrypoint.function.position", resourceFunctionPosition),
                    new AbstractMap.SimpleEntry<>("src.object.name", "ballerina/testobserve/Caller"),
                    new AbstractMap.SimpleEntry<>("src.function.name", "respond")
            ));
        });
    }
}
