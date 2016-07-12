/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.windowtable;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

public class TimeBatchWindowEventTableTestCase {
    private static final Logger log = Logger.getLogger(TimeBatchWindowEventTableTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;

    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
    }

    @Test
    public void testTimeWindowBatch1() throws InterruptedException {
        log.info("TimeWindowBatch Test1");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(1 sec); ";

        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventWindow " +
                "select symbol,sum(price) as sumPrice,volume " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEventCount == 0) {
                    Assert.assertTrue("Remove Events will only arrive after the second time period. ", removeEvents == null);
                }
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                } else if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(3000);
        Assert.assertEquals(1, inEventCount);
        Assert.assertEquals(1, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    @Test
    public void testTimeWindowBatch2() throws InterruptedException {
        log.info("TimeWindowBatch Test2");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(1 sec); ";

        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventWindow " +
                "select symbol, sum(price) as price " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    Assert.assertTrue("InEvents arrived before RemoveEvents", inEventCount > removeEventCount);
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(2000);
        Assert.assertEquals(3, inEventCount);
        Assert.assertEquals(1, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }


    @Test
    public void testTimeWindowBatch3() throws InterruptedException {
        log.info("TimeWindowBatch Test3");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(1 sec) output current events; ";

        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventWindow " +
                "select symbol, sum(price) as price " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                Assert.assertTrue("Remove events should not arrive ", removeEvents == null);
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(2000);
        Assert.assertEquals(3, inEventCount);
        Assert.assertEquals(0, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testTimeWindowBatch4() throws InterruptedException {
        log.info("TimeWindowBatch Test4");

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(1 sec) output expired events; ";

        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventWindow " +
                "select symbol, sum(price) as price " +
                "insert expired events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                }
                if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                Assert.assertTrue("inEvents should not arrive ", inEvents == null);
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 1});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 2});
        inputHandler.send(new Object[]{"IBM", 700f, 3});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 4});
        Thread.sleep(1100);
        inputHandler.send(new Object[]{"IBM", 700f, 5});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 6});
        Thread.sleep(2000);
        Assert.assertEquals(0, inEventCount);
        Assert.assertEquals(3, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testTimeWindowBatch5() throws InterruptedException {
        log.info("TimeWindowBatch Test5");

        SiddhiManager siddhiManager = new SiddhiManager();

        String streams = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define stream twitterStream (user string, tweet string, company string); " +
                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(2 sec); " +
                "define window twitterWindow (user string, tweet string, company string) timeBatch(5 sec); ";

        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from twitterStream " +
                "insert into twitterWindow; " +
                "" +
                "@info(name = 'query2') " +
                "from cseEventWindow join twitterWindow " +
                "on cseEventWindow.symbol== twitterWindow.company " +
                "select cseEventWindow.symbol as symbol, twitterWindow.tweet, cseEventWindow.price " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
        try {
            executionPlanRuntime.addCallback("query2", new QueryCallback() {
                @Override
                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                    EventPrinter.print(timeStamp, inEvents, removeEvents);
                    if (inEvents != null) {
                        inEventCount += (inEvents.length);
                    }
                    if (removeEvents != null) {
                        removeEventCount += (removeEvents.length);
                    }
                    eventArrived = true;
                }
            });
            InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
            InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
            executionPlanRuntime.start();
            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
            Thread.sleep(5000);
            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
            Thread.sleep(2000);
            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
            Thread.sleep(3500);

            Assert.assertEquals(2, inEventCount);
            Assert.assertEquals(2, removeEventCount);
            Assert.assertTrue(eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }
//
//    @Test
//    public void timeWindowBatchTest6() throws InterruptedException {
//        log.info("timeWindowBatch Test6");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//
//        String streams = "" +
//                "define stream cseEventStream (symbol string, price float, volume int); " +
//                "define stream twitterStream (user string, tweet string, company string); " +
//                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(1 sec) output current events; " +
//                "define window twitterWindow (user string, tweet string, company string) timeBatch(1 sec) output current events; ";
//
//        String query = "" +
//                "@info(name = 'query0') " +
//                "from cseEventStream " +
//                "insert into cseEventWindow; " +
//                "" +
//                "@info(name = 'query1') " +
//                "from twitterStream " +
//                "insert into twitterWindow; " +
//                "" +
//                "@info(name = 'query2') " +
//                "from cseEventWindow join twitterWindow " +
//                "on cseEventWindow.symbol== twitterWindow.company " +
//                "select cseEventWindow.symbol as symbol, twitterWindow.tweet, cseEventWindow.price " +
//                "insert into outputStream ;";
//
////        String streams = "" +
////                "define stream cseEventStream (symbol string, price float, volume int); " +
////                "define stream twitterStream (user string, tweet string, company string); ";
////        String query = "" +
////                "@info(name = 'query2') " +
////                "from cseEventStream#window.timeBatch(1 sec) join twitterStream#window.timeBatch(1 sec) " +
////                "on cseEventStream.symbol== twitterStream.company " +
////                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
////                "insert into outputStream ;";
//
//
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
//        try {
//            executionPlanRuntime.addCallback("query2", new QueryCallback() {
//                @Override
//                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                    EventPrinter.print(timeStamp, inEvents, removeEvents);
//                    if (inEvents != null) {
//                        inEventCount += (inEvents.length);
//                    }
//                    if (removeEvents != null) {
//                        removeEventCount += (removeEvents.length);
//                    }
//                    eventArrived = true;
//                }
//            });
//            InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
//            InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
//            executionPlanRuntime.start();
//            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
//            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
//            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
//            Thread.sleep(1500);
//            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
//            Thread.sleep(1100);
//            Assert.assertEquals(1, inEventCount);
//            Assert.assertEquals(0, removeEventCount);
//            Assert.assertTrue(eventArrived);
//        } finally {
//            executionPlanRuntime.shutdown();
//        }
//    }
//
//
//    @Test
//    public void timeWindowBatchTest60() throws InterruptedException {
//        log.info("timeWindowBatch Test6");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//
//        String streams = "" +
//                "define stream cseEventStream (symbol string, price float, volume int); " +
//                "define stream twitterStream (user string, tweet string, company string); ";
//        String query = "" +
//                "@info(name = 'query2') " +
//                "from cseEventStream#window.timeBatch(1 sec) join twitterStream#window.timeBatch(2 sec) " +
//                "on cseEventStream.symbol== twitterStream.company " +
//                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
//                "insert into outputStream ;";
//
//
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
//        try {
//            executionPlanRuntime.addCallback("query2", new QueryCallback() {
//                @Override
//                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                    EventPrinter.print(timeStamp, inEvents, removeEvents);
//                    if (inEvents != null) {
//                        inEventCount += (inEvents.length);
//                    }
//                    if (removeEvents != null) {
//                        removeEventCount += (removeEvents.length);
//                    }
//                    eventArrived = true;
//                }
//            });
//            InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
//            InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
//            executionPlanRuntime.start();
//            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
//            Thread.sleep(2100);
//            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
//            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
//            //Thread.sleep(2500);
//            //cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
//            Thread.sleep(100);
//            Assert.assertEquals(1, inEventCount);
//            Assert.assertEquals(0, removeEventCount);
//            Assert.assertTrue(eventArrived);
//        } finally {
//            executionPlanRuntime.shutdown();
//        }
//    }
//
//    @Test
//    public void LengthBatchWindowTest8() throws InterruptedException {
//        log.info("LengthBatchWindow Test8");
//
//        SiddhiManager siddhiManager = new SiddhiManager();
//        String streams = "" +
//                "define stream cseEventStream (symbol string, price float, volume int); " +
//                "define stream twitterStream (user string, tweet string, company string); ";
//        String query = "" +
//                "@info(name = 'query1') " +
//                "from cseEventStream#window.lengthBatch(2) join twitterStream#window.lengthBatch(2) " +
//                "on cseEventStream.symbol== twitterStream.company " +
//                "select cseEventStream.symbol as symbol, twitterStream.tweet, cseEventStream.price " +
//                "insert all events into outputStream ;";
//
//        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
//        try {
//            executionPlanRuntime.addCallback("query1", new QueryCallback() {
//                @Override
//                public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                    EventPrinter.print(timeStamp, inEvents, removeEvents);
//                    if (inEvents != null) {
//                        inEventCount+=(inEvents.length);
//                    }
//                    if (removeEvents != null) {
//                        removeEventCount+=(removeEvents.length);
//                    }
//                    eventArrived = true;
//                }
//            });
//            InputHandler cseEventStreamHandler = executionPlanRuntime.getInputHandler("cseEventStream");
//            InputHandler twitterStreamHandler = executionPlanRuntime.getInputHandler("twitterStream");
//            executionPlanRuntime.start();
//            cseEventStreamHandler.send(new Object[]{"WSO2", 55.6f, 100});
//            cseEventStreamHandler.send(new Object[]{"IBM", 59.6f, 100});
//            twitterStreamHandler.send(new Object[]{"User1", "Hello World", "WSO2"});
//            twitterStreamHandler.send(new Object[]{"User2", "Hello World2", "WSO2"});
////            cseEventStreamHandler.send(new Object[]{"IBM", 75.6f, 100});
////            Thread.sleep(500);
////            cseEventStreamHandler.send(new Object[]{"WSO2", 57.6f, 100});
//            Thread.sleep(1000);
//            Assert.assertEquals(4, inEventCount);
//            Assert.assertEquals(2, removeEventCount);
//            Assert.assertTrue(eventArrived);
//        } finally {
//            executionPlanRuntime.shutdown();
//        }
//    }

    @Test
    public void timeWindowBatchTest7() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int); " +
                "define window cseEventWindow (symbol string, price float, volume int) timeBatch(2 sec , 0); ";
        String query = "" +
                "@info(name = 'query0') " +
                "from cseEventStream " +
                "insert into cseEventWindow; " +
                "" +
                "@info(name = 'query1') " +
                "from cseEventWindow " +
                "select symbol, sum(price) as sumPrice, volume " +
                "insert into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEventCount == 0) {
                    Assert.assertTrue("Remove Events will only arrive after the second time period. ", removeEvents == null);
                }
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                } else if (removeEvents != null) {
                    removeEventCount = removeEventCount + removeEvents.length;
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(8000);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        inputHandler.send(new Object[]{"II", 60.5f, 1});
        Thread.sleep(13000);
        inputHandler.send(new Object[]{"TT", 60.5f, 1});
        inputHandler.send(new Object[]{"YY", 60.5f, 1});
        Thread.sleep(5000);
        Assert.assertEquals(3, inEventCount);
        Assert.assertEquals(0, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

}
