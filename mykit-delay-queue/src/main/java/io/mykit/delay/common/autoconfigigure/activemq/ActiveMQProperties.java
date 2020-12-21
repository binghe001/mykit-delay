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
package io.mykit.delay.common.autoconfigigure.activemq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/6/11
 * @description ActiveMQ的配置类
 */
@ConfigurationProperties(prefix = ActiveMQProperties.MYKIT_DELAY_ACTIVEMQ_PREFIX)
public class ActiveMQProperties {

    public static final String MYKIT_DELAY_ACTIVEMQ_PREFIX = "mykit.delay.activemq";

    private String brokerUrl;
    private String username;
    private String password;
    private boolean useExponentialBackOff;
    private boolean useAsyncSend;
    private int maximumRedeliveries;
    private int initialRedeliveryDelay;
    private int backOffMultiplier;
    private int maximumRedeliveryDelay;
    private boolean queue_pub_sub_domain;
    private boolean topic_pub_sub_domain;

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isUseExponentialBackOff() {
        return useExponentialBackOff;
    }

    public void setUseExponentialBackOff(boolean useExponentialBackOff) {
        this.useExponentialBackOff = useExponentialBackOff;
    }

    public boolean isUseAsyncSend() {
        return useAsyncSend;
    }

    public void setUseAsyncSend(boolean useAsyncSend) {
        this.useAsyncSend = useAsyncSend;
    }

    public int getMaximumRedeliveries() {
        return maximumRedeliveries;
    }

    public void setMaximumRedeliveries(int maximumRedeliveries) {
        this.maximumRedeliveries = maximumRedeliveries;
    }

    public int getInitialRedeliveryDelay() {
        return initialRedeliveryDelay;
    }

    public void setInitialRedeliveryDelay(int initialRedeliveryDelay) {
        this.initialRedeliveryDelay = initialRedeliveryDelay;
    }

    public int getBackOffMultiplier() {
        return backOffMultiplier;
    }

    public void setBackOffMultiplier(int backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public int getMaximumRedeliveryDelay() {
        return maximumRedeliveryDelay;
    }

    public void setMaximumRedeliveryDelay(int maximumRedeliveryDelay) {
        this.maximumRedeliveryDelay = maximumRedeliveryDelay;
    }

    public boolean isQueue_pub_sub_domain() {
        return queue_pub_sub_domain;
    }

    public void setQueue_pub_sub_domain(boolean queue_pub_sub_domain) {
        this.queue_pub_sub_domain = queue_pub_sub_domain;
    }

    public boolean isTopic_pub_sub_domain() {
        return topic_pub_sub_domain;
    }

    public void setTopic_pub_sub_domain(boolean topic_pub_sub_domain) {
        this.topic_pub_sub_domain = topic_pub_sub_domain;
    }
}
