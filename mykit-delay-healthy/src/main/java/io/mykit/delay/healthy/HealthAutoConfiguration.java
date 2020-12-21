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
package io.mykit.delay.healthy;

import io.mykit.delay.common.conf.AppEnvContext;
import io.mykit.delay.common.utils.Constants;
import io.mykit.delay.queue.leader.LeaderManager;
import io.mykit.delay.queue.leader.SimpleLeaderManager;
import io.mykit.delay.queue.redis.RedisQueue;
import io.mykit.delay.queue.redis.support.RedisQueueProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 健康检查自动配置
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE + 1000)
public class HealthAutoConfiguration {
    @Autowired
    private HealthAggregator healthAggregator;


    @Bean
    @Autowired(required = false)
    @ConditionalOnMissingBean
    public HealthIndicator jikexiuHealthIndicator(RedisQueue redisQueue, RedisQueueProperties properties) {
        CompositeHealthIndicator compositeHealthIndicator = new  CompositeHealthIndicator(healthAggregator);
        Map<String, LeaderManager> leaderManagerMap = AppEnvContext.getCtx().getBeansOfType(LeaderManager.class);
        LeaderManager manager = null;
        if (leaderManagerMap != null && !leaderManagerMap.isEmpty()) {
            manager = AppEnvContext.getCtx().getBean(SimpleLeaderManager.class);
        }

        compositeHealthIndicator.addHealthIndicator(Constants.HEALTH_INDICATOR_NAME, new QueueHealthIndicator(redisQueue, manager, properties));
        return compositeHealthIndicator;
    }
}
