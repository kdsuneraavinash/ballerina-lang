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

public class SegmentGeneratorActionsTest extends AbstractQuoterTest{

    @Test
    public void testBracedAction() {
        testAssertionFiles("actions/braced-action", "braced_action_source_01");
        testAssertionFiles("actions/braced-action", "braced_action_source_02");
    }

    @Test
    public void testCheckAction() {
        testAssertionFiles("actions/check-action", "check_action_source_01");
    }

    @Test
    public void testCommitAction() {
        testAssertionFiles("actions/commit-action", "commit_action_source_01");
    }

    @Test
    public void testQueryAction() {
        testAssertionFiles("actions/query-action", "query_action_source_01");
        testAssertionFiles("actions/query-action", "query_action_source_02");
        testAssertionFiles("actions/query-action", "query_action_source_04");
        testAssertionFiles("actions/query-action", "query_action_source_05");
    }

    @Test
    public void testReceiveAction() {
        // TODO incompatible types: ReceiveFieldsNode cannot be converted to SimpleNameReferenceNode?
        testAssertionFiles("actions/receive-action", "receive_action_source_01");
        testAssertionFiles("actions/receive-action", "receive_action_source_02");
        testAssertionFiles("actions/receive-action", "receive_action_source_03");
    }

    @Test
    public void testRemoteMethodCallAction() {
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_01");
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_02");
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_03");
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_05");
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_06");
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_07");
        testAssertionFiles("actions/remote-method-call-action", "remote_method_call_source_08");
    }

    @Test
    public void testSendAction() {
        testAssertionFiles("actions/send-action", "send_action_source_01");
        testAssertionFiles("actions/send-action", "send_action_source_02");
    }

    @Test
    public void testStartAction() {
        testAssertionFiles("actions/start-action", "start_action_source_01");
        testAssertionFiles("actions/start-action", "start_action_source_03");
    }

    @Test
    public void testTrapAction() {
        testAssertionFiles("actions/trap-action", "trap_action_source_01");
    }

    @Test
    public void testWaitAction() {
        testAssertionFiles("actions/wait-action", "wait_action_source_01");
        testAssertionFiles("actions/wait-action", "wait_action_source_02");
        testAssertionFiles("actions/wait-action", "wait_action_source_03");
        testAssertionFiles("actions/wait-action", "wait_action_source_05");
    }
}
