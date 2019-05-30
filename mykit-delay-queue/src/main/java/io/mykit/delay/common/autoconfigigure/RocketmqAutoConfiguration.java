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

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description RocketMQ注册
 */
@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
@ConditionalOnClass(value = {DefaultMQProducer.class})
public class RocketmqAutoConfiguration {

    @Autowired
    private RocketMQProperties properties;

    @Bean(initMethod = "init", destroyMethod = "close")
    public MessageProducer newMessageProducer() {
        Assert.notNull(properties.getNamesrvAddr(), "请正确配置NamesrvAddr");
        MessageProducer producer = new MessageProducer();
        producer.setNamesrvAddr(properties.getNamesrvAddr());
        return producer;
    }
}
