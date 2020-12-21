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
package io.mykit.delay.common.autoconfigigure.rocketmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description RocketMQ属性信息
 */
@ConfigurationProperties(prefix = RocketMQProperties.MYKIT_DELAY_ROCKETMQ_PREFIX)
public class RocketMQProperties {
    public static final String MYKIT_DELAY_ROCKETMQ_PREFIX = "mykit.delay.rocketmq";
    private             String namesrvAddr;
    private             String filterSourceRoot = "/home/";

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public String getFilterSourceRoot() {
        return filterSourceRoot;
    }

    public void setFilterSourceRoot(String filterSourceRoot) {
        this.filterSourceRoot = filterSourceRoot;
    }
}
