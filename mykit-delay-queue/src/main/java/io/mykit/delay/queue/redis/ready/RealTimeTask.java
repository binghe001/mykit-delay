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

import io.mykit.delay.common.utils.DateUtils;
import io.mykit.delay.common.utils.NamedUtil;
import io.mykit.delay.common.utils.Status;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.core.ConsumeQueueProvider;
import io.mykit.delay.queue.core.Queue;
import io.mykit.delay.queue.redis.JobOperationService;
import io.mykit.delay.queue.redis.event.JobEventBus;
import io.mykit.delay.queue.redis.event.RedisJobTraceEvent;
import io.mykit.delay.queue.redis.support.DistributedLock;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.TimerTask;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 实时Timer监控任务
 */
public class RealTimeTask extends TimerTask {

    public static final Logger               LOGGER               = LoggerFactory.getLogger(RealTimeTask.class);
    private RedisQueueProperties properties;
    private JobOperationService jobOperationService;
    private Queue delayQueue;
    private DistributedLock lock                 = null;
    private ConsumeQueueProvider consumeQueueProvider = null;

    @Override
    public void run() {
        runTemplate();
    }

    private void runTemplate() {
        if (properties.isCluster()) {
            String lockName = NamedUtil.buildLockName(NamedUtil.buildRealTimeName(properties.getPrefix(), properties.getName(), properties.getReadyName()));
            try {
                lock.lock(lockName);
                runInstance();
            } finally {
                lock.unlock(lockName);
            }
        } else {
            runInstance();
        }
    }

    private void runInstance() {
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("开始轮询实时队列...%s");
            }
            //获取ready队列中的一个数据
            List<String> jobIds = jobOperationService.getReadyJob(10);
            if (jobIds != null && jobIds.size() > 0) {
                for (String jobId : jobIds) {
                    if (!StringUtils.isEmpty(jobId)) {
                        JobMsg j = jobOperationService.getJob(jobId);
                        if (j == null) {
                            this.jobOperationService.removeReadyJob(jobId);
                            LOGGER.warn("任务ID {} 元数据池没有数据", jobId);
                            continue;
                        }
                        if (j.getStatus() == Status.Delete.ordinal()) {
                            this.jobOperationService.removeJobToPool(jobId);
                            continue;
                        }
                        if (j.getStatus() != Status.Delete.ordinal()) {
                            if (!check(j)) {//没有达到执行时间 从新发送延时Buck中
                                j.setStatus(Status.Restore.ordinal());
                                delayQueue.push(j);
                                continue;
                            }
                            if (LOGGER.isInfoEnabled()) {
                                long runLong = j.getDelay() + j.getCreateTime();
                                String runDateString = DateUtils.format(new Date(runLong), DateUtils.FORMAT_YYYY_MM_DD_HH_MM_SS_SSS);
                                LOGGER.info(String.format("invokeTask %s target time : %s", jobId, runDateString));
                            }
                            consumeQueueProvider.consumer(j);
                            j.setStatus(Status.Finish.ordinal());
                            this.jobOperationService.updateJobStatus(jobId, Status.Finish);
                            this.jobOperationService.removeReadyJob(jobId);
                            this.jobOperationService.removeJobToPool(jobId);
                            JobEventBus.getInstance().post(new RedisJobTraceEvent(j));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("处理实时队列发生错误", e);
        }
    }

    private boolean check(JobMsg job) {
        long runTime = job.getCreateTime() + job.getDelay(),
        currentTime = System.currentTimeMillis();
        return runTime <= currentTime;
    }


    public void setProperties(RedisQueueProperties properties) {
        this.properties = properties;
    }

    public void setJobOperationService(JobOperationService jobOperationService) {
        this.jobOperationService = jobOperationService;
    }

    /**
     * 注入队列操作对象 便于时间没有到触发条件下 执行重新发送
     */
    public void setDelayQueue(Queue delayQueue) {
        this.delayQueue = delayQueue;
    }

    public void setLock(DistributedLock lock) {
        this.lock = lock;
    }

    public void setConsumeQueueProvider(ConsumeQueueProvider consumeQueueProvider) {
        this.consumeQueueProvider = consumeQueueProvider;
    }
}
