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
package io.mykit.delay.queue.redis;

import io.mykit.delay.common.exception.DelayQueueException;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.core.Queue;
import io.mykit.delay.queue.redis.bucket.BucketQueueManager;
import io.mykit.delay.queue.redis.ready.ReadyQueueManager;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;

import java.io.Closeable;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Redis队列接口
 */
public interface RedisQueue extends Queue, Closeable {

    /**
     * 推动任务
     * @param job 任务实例
     * @throws DelayQueueException
     */
    void push(JobMsg job) throws DelayQueueException;

    /**
     * 设置属性信息
     * @param properties  Redis队列属性
     */
    void setProperties(RedisQueueProperties properties);

    /**
     * 设置任务操作服务接口
     * @param jobOperationService 任务操作服务接口
     */
    void setJobOperationService(JobOperationService jobOperationService);

    /**
     * 设置BucketQueue管理器
     * @param bucketQueueManager BucketQueue管理器
     */
    void setBucketQueueManager(BucketQueueManager bucketQueueManager);

    /**
     * 设置准备队列管理器
     * @param readyQueueManager 准备队列管理器
     */
    void setReadyQueueManager(ReadyQueueManager readyQueueManager);

    /**
     * 构建队列名称
     * @return  队列名称
     */
    String buildQueueName();
}
