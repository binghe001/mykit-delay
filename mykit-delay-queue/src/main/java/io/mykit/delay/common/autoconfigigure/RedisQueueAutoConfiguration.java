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
package io.mykit.delay.common.autoconfigigure;

import com.alibaba.druid.pool.DruidDataSource;
import io.mykit.delay.queue.redis.*;
import io.mykit.delay.queue.redis.bucket.BucketQueueManager;
import io.mykit.delay.queue.redis.event.JobEventBus;
import io.mykit.delay.queue.redis.event.JobEventListener;
import io.mykit.delay.queue.redis.event.RedisJobEventListener;
import io.mykit.delay.queue.redis.ready.ReadyQueueManager;
import io.mykit.delay.queue.redis.support.RedisDistributedLock;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import io.mykit.delay.queue.redis.support.RedisSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description Redis队列配置
 */
@Configuration
@EnableConfigurationProperties(RedisQueueProperties.class)
@ConditionalOnClass(value = {Jedis.class, RedisQueue.class})
public class RedisQueueAutoConfiguration {
    public static final Logger               LOGGER = LoggerFactory.getLogger(RedisQueueAutoConfiguration.class);
    @Autowired
    private             DruidConfig          druidConfig;
    @Autowired
    private             RedisQueueProperties properties;
    @Autowired
    private             StringRedisTemplate  template;

    private JobOperationService jobOperationService;


    @Bean
    public RedisSupport redisSupport() {
        RedisSupport support = new RedisSupport();
        support.setTemplate(template);
        return support;
    }

    /**
     * 分布式锁
     */
    @Bean
    @Autowired
    public RedisDistributedLock redisDistributedLock(RedisSupport redisSupport) {
        return new RedisDistributedLock(redisSupport);
    }

    @Bean
    @Autowired
    public JobOperationService JobOperationService(RedisSupport redisSupport) {
        JobOperationServiceImpl jobOperationService = new JobOperationServiceImpl();
        jobOperationService.setRedisSupport(redisSupport);
        jobOperationService.setProperties(properties);
        return jobOperationService;
    }

    @Bean
    @Autowired
    public BucketQueueManager BucketQueueManager(JobOperationService jobOperationService, RedisDistributedLock lock) {
        BucketQueueManager manager = new BucketQueueManager();
        manager.setProperties(properties);
        manager.setJobOperationService(jobOperationService);
        manager.setLock(lock);
        return manager;
    }

    @Bean
    public RdbStore rdbStore() {
        try {
            DruidDataSource ds = (DruidDataSource) druidConfig.newInstanceDruidDataSource();
            return new RdbStore(ds);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Bean
    @Autowired
    public JobEventListener jobEventListener(RdbStore store) {
        RedisJobEventListener eventListener = new RedisJobEventListener();
        eventListener.setStore(store);
        JobEventBus.getInstance().register(eventListener);
        return eventListener;
    }

    @Bean
    @Autowired
    public ReadyQueueManager readyQueueManager(JobOperationService jobOperationService, RedisDistributedLock lock) {
        ReadyQueueManager manager = new ReadyQueueManager();
        manager.setProperties(properties);
        manager.setJobOperationService(jobOperationService);
        manager.setLock(lock);
        return manager;
    }

    @Bean
    @Autowired
    public RedisQueue redisQueue(JobOperationService jobOperationService, BucketQueueManager bucketQueueManager, ReadyQueueManager readyQueueManager) {
        RedisQueue redisQueue = new RedisQueueImpl();
        redisQueue.setProperties(properties);
        redisQueue.setJobOperationService(jobOperationService);
        redisQueue.setBucketQueueManager(bucketQueueManager);
        redisQueue.setReadyQueueManager(readyQueueManager);
        readyQueueManager.setDelayQueue(redisQueue);
        return redisQueue;
    }
}
