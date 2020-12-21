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
package io.mykit.delay.rpc.dubbo;

import io.mykit.delay.common.utils.ResponseMessage;
import io.mykit.delay.queue.redis.JobWrapp;

/**
 * @author binghe
 * @version 1.0.0
 * @description 发布的Dubbo接口
 */
public interface MykitDelayDubboInterface {

    /**
     * 推送消息
     */
    ResponseMessage push(JobWrapp jobMsg);

    /**
     * 删除任务
     */
    ResponseMessage delete(String jobId);


    /**
     * 完成任务
     */
    ResponseMessage finish(String jobId);

    /**
     * 恢复单个任务
     */
    ResponseMessage reStoreJob(String jobId);

    /**
     * 提供一个方法 假设缓存中间件出现异常 以及数据错乱的情况 提供恢复功能
     * @param expire 过期的数据是否需要重发 true需要, false不需要 默认为true
     */
    ResponseMessage reStore(Boolean expire);

    /**
     * 清除所有的任务
     */
    ResponseMessage clearAll();
}
