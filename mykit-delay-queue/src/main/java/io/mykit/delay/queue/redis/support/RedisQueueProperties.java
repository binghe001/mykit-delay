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
package io.mykit.delay.queue.redis.support;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Redis队列配置属性
 */
@ConfigurationProperties(prefix = RedisQueueProperties.REDIS_QUEUE_PREFIX)
public class RedisQueueProperties {
    public static final String REDIS_QUEUE_PREFIX = "mykit.delay.rqueue";
    private             String name;
    private             String prefix             = "io.mykit.delay";
    private             String originPool         = "pools";
    private             String readyName          = "ready";
    private             int    bucketSize         = 3;

    /**
     * buck轮询时间
     **/
    private long    buckRoundRobinTime  = 300;
    /**
     * ready轮询时间
     **/
    private long    readyRoundRobinTime = 200;
    private boolean cluster             = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getOriginPool() {
        return originPool;
    }

    public void setOriginPool(String originPool) {
        this.originPool = originPool;
    }

    public String getReadyName() {
        return readyName;
    }

    public void setReadyName(String readyName) {
        this.readyName = readyName;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    public void setBucketSize(int bucketSize) {
        this.bucketSize = bucketSize;
    }

    public boolean isCluster() {
        return cluster;
    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public long getBuckRoundRobinTime() {
        if (buckRoundRobinTime <= 0) {
            buckRoundRobinTime = 500;
        }
        return buckRoundRobinTime;
    }

    public void setBuckRoundRobinTime(long buckRoundRobinTime) {
        this.buckRoundRobinTime = buckRoundRobinTime;
    }

    public long getReadyRoundRobinTime() {
        if (readyRoundRobinTime <= 0) {
            readyRoundRobinTime = 500;
        }
        return readyRoundRobinTime;
    }

    public void setReadyRoundRobinTime(long readyRoundRobinTime) {
        this.readyRoundRobinTime = readyRoundRobinTime;
    }
}
