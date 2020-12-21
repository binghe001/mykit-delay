/**
 * Copyright 2020-9999 the original author or authors.
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
package io.mykit.delay.dubbo.client;

import io.mykit.delay.queue.core.ConsumeQueueProvider;
import io.mykit.delay.queue.redis.JobWrapp;
import io.mykit.delay.queue.redis.RedisQueue;
import io.mykit.delay.starter.ready.StartGetReady;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.UUID;

/**
 * @author binghe
 * @version 1.0.0
 * @description dubbo测试客户端
 */
public class MykitDelayClient {

    public static void main(String[] args){
        StartGetReady.ready(ConsumeQueueProvider.class.getName());
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/mykit-delay-server-client.xml");
        RedisQueue redisQueue = (RedisQueue) context.getBean("redisQueue");
        JobWrapp jobWrapp = new JobWrapp();
        jobWrapp.setId(UUID.randomUUID().toString());
        jobWrapp.setBuckedName("test");
        jobWrapp.setBizKey("testCode");
        jobWrapp.setBody("测试定时调度");
        jobWrapp.setDelay(1800);
        jobWrapp.setCreateTime(System.currentTimeMillis());
        redisQueue.push(jobWrapp);
    }

}
