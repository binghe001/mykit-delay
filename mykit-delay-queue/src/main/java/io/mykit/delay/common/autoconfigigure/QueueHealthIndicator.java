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

import io.mykit.delay.common.utils.Constants;
import io.mykit.delay.queue.core.Queue;
import io.mykit.delay.queue.leader.LeaderManager;
import io.mykit.delay.queue.leader.ServerNode;
import io.mykit.delay.queue.redis.RedisQueue;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 队列健康注册器
 */
public class QueueHealthIndicator implements HealthIndicator {
    private Queue queue;
    private LeaderManager leaderManager;
    private RedisQueueProperties redisQueueProperties;

    public QueueHealthIndicator(RedisQueue queue, LeaderManager leaderManager, RedisQueueProperties
            redisQueueProperties) {
        this.queue = queue;
        this.leaderManager = leaderManager;
        this.redisQueueProperties = redisQueueProperties;
    }

    @Override
    public Health health() {
        try {
            Health.Builder builder = Health.up();
            if (leaderManager == null) {
                builder.withDetail(Constants.RUN, queue.isRunning());
            } else {
                builder.withDetail(Constants.RUN, queue.isRunning()).withDetail(Constants.IS_MASTER, leaderManager.isLeader());
            }
            return builder
                    .withDetail(Constants.IS_CLUSTER, redisQueueProperties.isCluster())
                    .withDetail(Constants.BUCKET_SIZE, redisQueueProperties.getBucketSize())
                    .withDetail(Constants.PREFIX, redisQueueProperties.getPrefix())
                    .withDetail(Constants.NAMESPACE, ServerNode.NAMESPACE)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
