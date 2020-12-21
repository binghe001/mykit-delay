/**
 * Copyright 2019-2999 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mykit.delay.common.autoconfigigure.message.activemq;

import io.mykit.delay.queue.activemq.ActiveMQSender;
import io.mykit.delay.queue.activemq.ActiveMQSenderFactory;
import io.mykit.delay.queue.activemq.QueueSender;
import io.mykit.delay.queue.activemq.TopicSender;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/6/12
 * @description 配置ActiveMQ
 */
@Configuration
@EnableConfigurationProperties(ActiveMQProperties.class)
public class ActiveMQAutoConfiguration {

    @Autowired
    private ActiveMQProperties properties;

    @Bean
    public RedeliveryPolicy activeMQRedeliveryPolicy(){
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setUseExponentialBackOff(properties.isUseExponentialBackOff());
        policy.setMaximumRedeliveries(properties.getMaximumRedeliveries());
        policy.setInitialRedeliveryDelay(properties.getInitialRedeliveryDelay());
        policy.setBackOffMultiplier(properties.getBackOffMultiplier());
        policy.setMaximumRedeliveryDelay(properties.getMaximumRedeliveryDelay());
        return policy;
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(){
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(properties.getBrokerUrl());
        activeMQConnectionFactory.setUserName(properties.getUsername());
        activeMQConnectionFactory.setPassword(properties.getPassword());
        activeMQConnectionFactory.setUseAsyncSend(properties.isUseAsyncSend());
        activeMQConnectionFactory.setRedeliveryPolicy(activeMQRedeliveryPolicy());
        return activeMQConnectionFactory;
    }

    @Bean
    public PooledConnectionFactory  connectionFactory(){
        PooledConnectionFactory factory = new PooledConnectionFactory();
        factory.setConnectionFactory(activeMQConnectionFactory());
        return factory;
    }

    @Bean
    public JmsTemplate jmsQueueTemplate(){
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setPubSubDomain(properties.isQueue_pub_sub_domain());
        return jmsTemplate;
    }

    @Bean
    public JmsTemplate jmsTopicTemplate(){
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setPubSubDomain(properties.isTopic_pub_sub_domain());
        return jmsTemplate;
    }

    @Bean
    public ActiveMQSender queueSender(){
        ActiveMQSender queueSender = new QueueSender(jmsQueueTemplate());
        ActiveMQSenderFactory.put(ActiveMQSenderFactory.JMS_QUEUE_SENDER, queueSender);
        return queueSender;
    }

    @Bean
    public ActiveMQSender topicSender(){
        ActiveMQSender topicSender = new TopicSender(jmsTopicTemplate());
        ActiveMQSenderFactory.put(ActiveMQSenderFactory.JMS_TOPIC_SENDER, topicSender);
        return topicSender;
    }
}
