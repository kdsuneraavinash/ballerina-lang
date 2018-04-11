/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.queue;

import io.ballerina.messaging.broker.common.ResourceNotFoundException;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.ValidationException;
import io.ballerina.messaging.broker.common.config.BrokerCommonConfiguration;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.common.data.types.FieldTable;
import io.ballerina.messaging.broker.common.data.types.FieldValue;
import io.ballerina.messaging.broker.core.Binding;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerException;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.core.Consumer;
import io.ballerina.messaging.broker.core.ContentChunk;
import io.ballerina.messaging.broker.core.Message;
import io.ballerina.messaging.broker.core.Metadata;
import io.ballerina.messaging.broker.core.configuration.BrokerCoreConfiguration;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class providing utility methods to interact with the broker operating in in-memory mode.
 * TODO: Needs refactoring once the broker core is moved to the Ballerina core
 *
 * @since 0.965.0
 */
public class BrokerUtils {

    private static final Logger logger = LoggerFactory.getLogger(BrokerUtils.class);

    private static Broker broker;

    static {
        try {
            broker = startupBroker();
        } catch (Exception e) {
            throw new BallerinaException("Error starting up in-memory broker: ", e);
        }
    }

    /**
     * Method to add a subscription to the broker operating in in-memory mode.
     *
     * @param topic     the topic for which the subscription is registered
     * @param consumer  the consumer to register for the subscription
     */
    public static void addSubscription(String topic, Consumer consumer, String selector) {
        String queueName = consumer.getQueueName(); //need to rely on implementers of consumers to specify unique names
        try {
            if (broker.getQueue(topic) == null) {
                broker.createQueue(queueName, false, false, true);
            }

            FieldTable selectorEntry = new FieldTable();
            selectorEntry.add(Binding.JMS_SELECTOR_ARGUMENT, FieldValue.parseLongString(selector));
            broker.bind(queueName, "amq.topic", topic, selectorEntry);
            broker.addConsumer(consumer);
        } catch (BrokerException | ValidationException e) {
            logger.error("Error adding subscription: ", e);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void addSubscription(String topic, Consumer consumer) {
        String queueName = consumer.getQueueName(); //need to rely on implementers of consumers to specify unique names
        try {
            if (broker.getQueue(topic) == null) {
                broker.createQueue(queueName, false, false, true);
            }
            broker.bind(queueName, "amq.topic", topic, FieldTable.EMPTY_TABLE);
            broker.addConsumer(consumer);
        } catch (BrokerException | ValidationException e) {
            logger.error("Error adding subscription: ", e);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to remove a subscription to the broker operating in in-memory mode.
     *
     * @param consumer  the consumer representing the subscription to remove
     */
    public static void removeSubscription(Consumer consumer) {
        broker.removeConsumer(consumer);
    }

    /**
     * Method to publish to a topic in the broker operating in in-memory mode.
     *
     */
    public static void publish(Message message) throws BrokerException {
        broker.publish(message);
    }

    /**
     * Method to start up the Ballerina Broker. TODO: change
     */
    private static Broker startupBroker() throws Exception {
        BallerinaBrokerConfigProvider configProvider = new BallerinaBrokerConfigProvider();
        BrokerCommonConfiguration brokerCommonConfiguration = new BrokerCommonConfiguration();
        brokerCommonConfiguration.setEnableInMemoryMode(true);
        configProvider.registerConfigurationObject(BrokerCommonConfiguration.NAMESPACE, brokerCommonConfiguration);
        BrokerCoreConfiguration brokerCoreConfiguration = new BrokerCoreConfiguration();
        brokerCoreConfiguration.setDurableQueueInMemoryCacheLimit("1000");
        configProvider.registerConfigurationObject(BrokerCoreConfiguration.NAMESPACE, brokerCoreConfiguration);
        StartupContext startupContext = new StartupContext();
        startupContext.registerService(BrokerConfigProvider.class, configProvider);
        Broker brokerInstance = new BrokerImpl(startupContext);
        brokerInstance.startMessageDelivery();
        return brokerInstance;
    }

    /**
     * Configuration provider implementation.
     */
    private static class BallerinaBrokerConfigProvider implements BrokerConfigProvider {

        private Map<String, Object> configMap = new HashMap<>();

        @Override
        public <T> T getConfigurationObject(String namespace, Class<T> configurationClass) throws Exception {
            return configurationClass.cast(configMap.get(namespace));
        }

        void registerConfigurationObject(String namespace, Object configObject) {
            configMap.put(namespace, configObject);
        }
    }

    public static Message createMessage(String topic, byte[] payload) {
        Message message = new Message(Broker.getNextMessageId(), new Metadata(topic, "amq.topic", payload.length));
        ByteBuf content = Unpooled.copiedBuffer(payload);
        message.addChunk(new ContentChunk(0, content));
        return message.shallowCopyWith(Broker.getNextMessageId(), topic, "amq.topic");
    }
}
