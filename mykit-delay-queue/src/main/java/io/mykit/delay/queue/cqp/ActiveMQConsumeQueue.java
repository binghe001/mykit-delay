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
package io.mykit.delay.queue.cqp;

import io.mykit.delay.common.exception.ConsumeQueueException;
import io.mykit.delay.queue.activemq.ActiveMQSender;
import io.mykit.delay.queue.activemq.ActiveMQSenderFactory;
import io.mykit.delay.queue.core.ConsumeQueueProvider;
import io.mykit.delay.queue.core.Job;
import io.mykit.delay.queue.extension.ExtNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/6/12
 * @description ActiveMQ 消费队列
 */
@ExtNamed("activemqCQ")
public class ActiveMQConsumeQueue implements ConsumeQueueProvider {
    private final Logger logger = LoggerFactory.getLogger(ActiveMQConsumeQueue.class);
    @Override
    public void init() {

    }

    @Override
    public void consumer(Job job) throws ConsumeQueueException {
        ActiveMQSender activeMQSender =  ActiveMQSenderFactory.getActiveMQSender(ActiveMQSenderFactory.JMS_QUEUE_SENDER);
        if (activeMQSender != null){
            activeMQSender.send(job.getTopic(), job.toJsonString());
        }else{
            logger.info("未获取到队列发送句柄....");
        }
    }

    @Override
    public void destory() {

    }
}
