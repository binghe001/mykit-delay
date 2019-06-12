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

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/6/12
 * @description ActiveMQ发送者工厂类
 *              主要是项目启动的时候缓存发送消息的句柄QueueSender和TopicSender, 供实际延时到时向ActiveMQ推送消息使用
 *              前提是要在starter.properties中的default.cq 配置为 activemqCQ
 */
public class ActiveMQSenderFactory {

    /**
     * 点对点队列模式
     */
    public static final String JMS_QUEUE_SENDER = "queueSender";

    /**
     * 发布/订阅模式
     */
    public static final String JMS_TOPIC_SENDER = "topicSender";

    /**
     * 存放发送消息句柄QueueSender和TopicSender的缓存结构
     */
    private volatile static Map<String, ActiveMQSender> instance;

    static {
        instance = new HashMap<String, ActiveMQSender>();
    }

    /**
     * 将发送数据的句柄放入缓存
     * @param key 缓存中的key
     * @param activeMQSender 发送消息的句柄
     */
    public static void put(String key, ActiveMQSender activeMQSender){
        instance.put(key, activeMQSender);
    }

    /**
     * 获取缓存中的发送消息句柄
     * @param key 缓存中的key
     * @return 发送消息句柄
     */
    public static ActiveMQSender getActiveMQSender(String key){
        return instance.get(key);
    }
}
