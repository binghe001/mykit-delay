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
package io.mykit.delay.queue.redis.ready;
import io.mykit.delay.queue.core.ConsumeQueueProvider;
import io.mykit.delay.queue.core.Queue;
import io.mykit.delay.queue.extension.ExtensionLoader;
import io.mykit.delay.queue.redis.JobOperationService;
import io.mykit.delay.queue.redis.support.DistributedLock;
import io.mykit.delay.queue.redis.support.Lifecycle;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Redis队列管理器
 */
public class ReadyQueueManager  implements Lifecycle {

    public static final Logger               LOGGER      = LoggerFactory.getLogger(ReadyQueueManager.class);
    public static final String               THREAD_NAME = "mykit-ready-queue-%s";
    public              boolean              daemon      = true;
    private volatile    AtomicBoolean        isRuning    = new AtomicBoolean(false);
    private RedisQueueProperties properties;
    private             Timer                timer;
    private JobOperationService jobOperationService;
    private Queue delayQueue;
    private             String               threadName;
    private DistributedLock lock        = null;


    @Override
    public void start() {
        if (isRuning.compareAndSet(false, true)) {
            threadName = String.format(THREAD_NAME, 1);
            timer = new Timer(threadName, daemon);
            RealTimeTask task = new RealTimeTask();
            task.setProperties(properties);
            task.setJobOperationService(jobOperationService);
            task.setDelayQueue(delayQueue);
            task.setLock(lock);
            task.setConsumeQueueProvider(ExtensionLoader.getExtension(ConsumeQueueProvider.class));
            timer.schedule(task, 500, properties.getReadyRoundRobinTime());
            LOGGER.info(String.format("Starting Ready Thead %s ....", threadName));
        }
    }

    @Override
    public void stop() {
        if (isRuning.compareAndSet(true, false)) {
            if (timer != null) {
                timer.cancel();
                LOGGER.info(String.format("stoping timer %s .....", threadName));
            }
        }
    }

    @Override
    public boolean isRunning() {
        return isRuning.get();
    }

    public void setProperties(RedisQueueProperties properties) {
        this.properties = properties;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public void setDelayQueue(Queue delayQueue) {
        this.delayQueue = delayQueue;
    }

    public void setJobOperationService(JobOperationService jobOperationService) {
        this.jobOperationService = jobOperationService;
    }

    public void setLock(DistributedLock lock) {
        this.lock = lock;
    }
}
