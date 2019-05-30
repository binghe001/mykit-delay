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
import io.mykit.delay.common.exception.JobNotFoundException;
import io.mykit.delay.common.utils.NamedUtil;
import io.mykit.delay.common.utils.RdbOperation;
import io.mykit.delay.common.utils.Status;
import io.mykit.delay.queue.JobMsg;
import io.mykit.delay.queue.redis.bucket.BucketQueueManager;
import io.mykit.delay.queue.redis.event.JobEventBus;
import io.mykit.delay.queue.redis.event.RedisJobTraceEvent;
import io.mykit.delay.queue.redis.ready.ReadyQueueManager;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Redis队列实现类
 */
public class RedisQueueImpl implements RedisQueue {

    public static final Logger        LOGGER   = LoggerFactory.getLogger(RedisQueueImpl.class);
    private volatile    AtomicBoolean isRuning = new AtomicBoolean(false);
    private volatile    AtomicInteger pos      = new AtomicInteger(0);

    private JobOperationService jobOperationService;

    private RedisQueueProperties properties;

    private BucketQueueManager bucketQueueManager;

    private ReadyQueueManager readyQueueManager;

    @Override
    public void push(JobMsg job) throws DelayQueueException {
        try {
            Assert.notNull(job, "Job不能为空");
            Assert.notNull(job.getId(), "JobId 不能为空");
            Assert.notNull(job.getDelay(), "Job Delay不能为空");
            if (job.getStatus() != Status.WaitPut.ordinal() && job.getStatus() != Status.Restore.ordinal()) {
                throw new IllegalArgumentException(String.format("任务%s状态异常", job.getId()));
            }
            String queueName = buildQueueName();
            if (job instanceof JobWrapp) {
                ((JobWrapp) job).setBuckedName(queueName);
            }
            this.jobOperationService.addJobToPool(job);
            JobEventBus.getInstance().post(new RedisJobTraceEvent(job, RdbOperation.INSERT));
            double score = Long.valueOf(job.getCreateTime() + job.getDelay());

            this.jobOperationService.addBucketJob(queueName, job.getId(), score);
            job.setStatus(Status.Delay.ordinal());
            this.jobOperationService.updateJobStatus(job.getId(), Status.Delay);
            JobEventBus.getInstance().post(new RedisJobTraceEvent(job));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("task {} append success bucket to {} !", job.getId(), queueName);
            }
        } catch (Exception e) {
            LOGGER.error("添加任务失败", e);
            throw new DelayQueueException(e);
        }
    }


    @Override
    public boolean ack(String jobMsgId) throws DelayQueueException {
        throw new DelayQueueException("待实现");
    }

    @Override
    public long getSize() {
        throw new DelayQueueException("待实现");
    }

    @Override
    public void clear() {
        LOGGER.warn("正在执行清空队列操作 请注意");
        this.jobOperationService.clearAll();
    }

    @Override
    public boolean delete(String jobMsgId) {
        JobWrapp job = (JobWrapp) this.jobOperationService.getJob(jobMsgId);

        if (null == job) {
            return false;
        }
        if (job.getStatus() == Status.Finish.ordinal()) {
            throw new JobNotFoundException(String.format("任务 %s 已经完成", jobMsgId));
        }
        job.setStatus(Status.Delete.ordinal());
        this.jobOperationService.addJobToPool(job);//更新这个数据到池
        JobEventBus.getInstance().post(new RedisJobTraceEvent(job));
        //是否需要删除buck？
        if (!StringUtils.isEmpty(job.getBuckedName())) {
            this.jobOperationService.removeBucketKey(job.getBuckedName(), jobMsgId);
            this.jobOperationService.removeJobToPool(jobMsgId);//fix 删除源数据 这种放在源数据池中毫无意义
        }
        return true;
    }

    @Override
    public JobMsg getJob(String jobId) {
        Assert.notNull(jobId);
        JobMsg jobMsg = this.jobOperationService.getJob(jobId);
        return jobMsg;
    }

    @Override
    public String getImplementType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    /////////////////////////help ////////////

    /**
     * 根据BuckSize轮询获取
     */
    @Override
    public String buildQueueName() {
        return NamedUtil.buildBucketName(properties.getPrefix(), properties.getName(), getNextRoundRobin());
    }

    /**
     * 轮询算法  目前只适用于单机
     */
    private int getNextRoundRobin() {
        synchronized (this) {
            if (pos.get() >= properties.getBucketSize() || pos.get() < 0) {
                pos.set(1);
            } else {
                pos.getAndIncrement();
            }
        }
        return pos.get();
    }

    @Override
    public void setJobOperationService(JobOperationService jobOperationService) {
        this.jobOperationService = jobOperationService;
    }

    @Override
    public void setProperties(RedisQueueProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setBucketQueueManager(BucketQueueManager bucketQueueManager) {
        this.bucketQueueManager = bucketQueueManager;
    }

    @Override
    public void setReadyQueueManager(ReadyQueueManager readyQueueManager) {
        this.readyQueueManager = readyQueueManager;
    }

    @Override
    public void start() {
        if (isRuning.compareAndSet(false, true)) {
            if (LOGGER.isInfoEnabled() && properties.isCluster()) {
                LOGGER.info("Cluster Model Starting...");
            }
            bucketQueueManager.start();
            readyQueueManager.start();
        }
    }

    @Override
    public void stop() {
        if (isRuning.compareAndSet(true, false)) {
            bucketQueueManager.stop();
            readyQueueManager.stop();
        }

    }

    @Override
    public boolean isRunning() {
        return isRuning.get();
    }
}
