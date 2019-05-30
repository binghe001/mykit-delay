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
package io.mykit.delay.queue.leader;

import io.mykit.delay.common.autoconfigigure.RegistryProperties;
import io.mykit.delay.common.utils.BlockUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description
 */
public class SimpleLeaderManager implements LeaderManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(SimpleLeaderManager.class);

    private LeaderLatch leaderLatch;

    private CuratorFramework framework;

    private String serverName = "";

    private volatile AtomicBoolean isLatch = new AtomicBoolean(false);
    private List<LeaderLatchListener> listeners = Lists.newArrayList();
    private RegistryProperties properties;

    public void init() {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(properties.getServerList())
                .retryPolicy(new ExponentialBackoffRetry(properties.getBaseSleepTimeMilliseconds(),
                        properties.getMaxRetries(),
                        properties.getMaxSleepTimeMilliseconds()))
                .namespace(ServerNode.NAMESPACE);
        framework = builder.build();
        framework.start();
        leaderLatch = new LeaderLatch(framework, ServerNode.LEADERLATCH, serverName, LeaderLatch.CloseMode.NOTIFY_LEADER);
        for (LeaderLatchListener listener : listeners) {
            leaderLatch.addListener(listener);
        }
        LOGGER.info("starting Queue Master Slave Model ...");
        start();
    }


    @Override
    public void start() {
        if (isLatch.compareAndSet(false, true)) {
            try {
                LOGGER.info("starting latch....");
                leaderLatch.start();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void stop() {
        if (isLatch.compareAndSet(true, false)) {
            try {
                BlockUtils.sleep(500);
                LOGGER.info("stop latch....");
                CloseableUtils.closeQuietly(leaderLatch);
                //CloseableUtils.closeQuietly(framework);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return isLatch.get();
    }

    @Override
    public boolean isLeader() {
        return leaderLatch.hasLeadership();
    }

    public void setProperties(RegistryProperties properties) {
        this.properties = properties;
    }

    public void addListener(LeaderLatchListener leaderLatchListener) {
        if (!listeners.contains(leaderLatchListener)) {
            listeners.add(leaderLatchListener);
        }

    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
