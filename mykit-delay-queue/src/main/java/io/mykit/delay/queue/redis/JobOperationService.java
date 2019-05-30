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

import io.mykit.delay.common.utils.Status;
import io.mykit.delay.queue.JobMsg;

import java.util.List;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 任务操作服务接口
 */
public interface JobOperationService {
    /**
     * 获取Job元数据
     */
    JobMsg getJob(String jobId);

    /**
     * 添加Job到元数据池
     */
    void addJobToPool(JobMsg jobMsg);

    /**
     * 删除元数据此任务
     */
    void removeJobToPool(String jobId);

    /**
     * 更新元任务池任务的状态
     */
    void updateJobStatus(String jobId, Status status);

    /**
     * 根据JobId删除元数据
     */
    void deleteJobToPool(String jobId);

    /**
     * 加一个Job到指定Bucket
     */
    void addBucketJob(String bucketName, String JobId, double score);

    /**
     * 从指定Bucket删除一个Job
     */
    void removeBucketKey(String bucketName, String jobId);

    /**
     * 添加一个Job到 可执行队列
     */
    void addReadyTime(String readyName, String jobId);


    /**
     * 获取一个实时队列中的第一个数据
     */
    String getReadyJob();

    /**
     * 获取指定个数实时队列中的数据 不是用的POP方式 需要手動刪除
     */
    List<String> getReadyJob(int size);

    /**
     * 刪除实时队列中的一个数据
     */
    boolean removeReadyJob(String jobId);

    /**
     * 获取bucket中最顶端的一个Job
     */
    String getBucketTop1Job(String bucketName);

    /**
     * 批量获取顶端数据 只获取满足条件的数据 最多<code>size</code>行
     */
    List<String> getBucketTopJobs(String bucketName, int size);

    /**
     * 清空所有任务
     */
    void clearAll();
}
