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

import io.mykit.delay.queue.redis.support.DistributedLock;
import io.mykit.delay.queue.redis.support.RedisDistributedLock;
import io.mykit.delay.queue.redis.support.RedisSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 测试分布式锁
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedLockTest {

    public static final Logger   LOGGER = LoggerFactory.getLogger(DistributedLockTest.class);
    @Autowired
    private RedisSupport redisSupport;

    @Test
    public void test1() {
        final DistributedLock lock            = new RedisDistributedLock(redisSupport);
        ExecutorService       executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 20; i++) {
            final int index = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (index >= 10) {
                            lock.lock("test002");
                        } else {
                            lock.lock("test001");
                        }

                        LOGGER.info("我得到锁了 {} ", index);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (index >= 10) {
                            lock.unlock("test002");
                        } else {
                            lock.unlock("test001");
                        }
                    }
                }
            });

        }
        try {
            Thread.sleep(1000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
