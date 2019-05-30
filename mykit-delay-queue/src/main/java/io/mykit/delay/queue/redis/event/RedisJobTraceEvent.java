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
package io.mykit.delay.queue.redis.event;

import io.mykit.delay.common.utils.RdbOperation;
import io.mykit.delay.queue.JobMsg;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Redis Job 栈事件
 */
public class RedisJobTraceEvent  implements JobEvent {

    private JobMsg jobMsg    = null;
    private RdbOperation operation = RdbOperation.UPDATE;

    public RedisJobTraceEvent(JobMsg jobMsg) {
        this.jobMsg = jobMsg;
    }

    public RedisJobTraceEvent(JobMsg jobMsg, RdbOperation operation) {
        this.jobMsg = jobMsg;
        this.operation = operation;
    }

    @Override
    public JobMsg getJob() {
        return jobMsg;
    }

    public RdbOperation getOperation() {
        return operation;
    }
}
