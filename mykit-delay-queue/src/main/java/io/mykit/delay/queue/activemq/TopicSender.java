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
package io.mykit.delay.queue.activemq;

import org.apache.activemq.ScheduledMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/6/12
 * @description Topic生产者发送消息到Topic
 */
public class TopicSender implements ActiveMQSender{

    private JmsTemplate jmsTemplate;

    public TopicSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * 发送一条消息到指定的队列（目标）
     * @param topicName 队列名称
     * @param message 消息内容
     */
    @Override
    public void send(String topicName,final String message){
        jmsTemplate.send(topicName, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }

    /**
     * 发送一条消息到指定的队列（目标）
     * @param topicName 队列名称
     * @param message 消息内容
     * @param scheduledDelayTime 间隔时间
     */
    @Override
    public void send(String topicName, final String message, final long scheduledDelayTime) {
        jmsTemplate.send(topicName, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage msg = session.createTextMessage(message);
                msg.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, scheduledDelayTime);
                return msg;
            }
        });
    }
}
