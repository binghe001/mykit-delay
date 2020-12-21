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
package io.mykit.delay.common.autoconfigigure.ha;

import io.mykit.delay.common.autoconfigigure.RegistryProperties;
import io.mykit.delay.common.conf.AppEnvContext;
import io.mykit.delay.common.utils.IpUtils;
import io.mykit.delay.queue.leader.LeaderManager;
import io.mykit.delay.queue.leader.LeaderWorkListener;
import io.mykit.delay.queue.leader.ServerNode;
import io.mykit.delay.queue.leader.SimpleLeaderManager;
import io.mykit.delay.queue.redis.RedisQueue;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description Leader配置
 */
@Configuration
@EnableConfigurationProperties(RegistryProperties.class)
@ConditionalOnProperty(prefix = RegistryProperties.MYKIT_DELAY_REGISTRY_PREFIX, value = "enable", havingValue = "true")
@ConditionalOnClass(value = {ZooKeeperServer.class, CuratorFrameworkFactory.class})
@Order(Ordered.LOWEST_PRECEDENCE + 50)
public class LeaderAutoConfiguration {

    @Autowired
    private RegistryProperties registryProperties;


    @Bean
    @Autowired
    @ConditionalOnMissingBean
    public LeaderLatchListener leaderLatchListenerImpl(RedisQueue redisQueue) {
        LeaderWorkListener listener = new LeaderWorkListener();
        listener.setQueue(redisQueue);
        return listener;
    }

    @Bean(name = "simpleLeaderManager", initMethod = "init", destroyMethod = "stop")
    @Autowired
    @ConditionalOnMissingBean
    public LeaderManager leaderManager(LeaderLatchListener leaderLatchListener) {
        SimpleLeaderManager slm = new SimpleLeaderManager();
        slm.setProperties(registryProperties);
        slm.addListener(leaderLatchListener);
        ServerNode.NAMESPACE = registryProperties.getNamespace();
        slm.setServerName(IpUtils.getIp() + ":" + AppEnvContext.getProperty("server.port"));
        return slm;
    }
}
