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
package io.mykit.delay.test;

import io.mykit.delay.queue.redis.RedisQueue;
import io.mykit.delay.queue.redis.RedisQueueImpl;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description
 */
public class RoundRobinTest {

    public static void main(String[] args) {
        RedisQueueProperties properties = new RedisQueueProperties();
        properties.setBucketSize(1);
        properties.setPrefix("io.mykit.delay");
        properties.setName("b");
        final RedisQueue redisQueue = new RedisQueueImpl();
        redisQueue.setProperties(properties);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                    System.out.println(redisQueue.buildQueueName());
                }
            }
        }).start();
    }

}
