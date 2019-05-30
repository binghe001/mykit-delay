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
package io.mykit.delay.common.listener;

import io.mykit.delay.common.conf.AppEnvContext;
import io.mykit.delay.common.utils.Constants;
import io.mykit.delay.queue.redis.RedisQueue;
import io.mykit.delay.queue.redis.RedisQueueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 启动事件监听器
 */
@Configuration
public class StartEventListener implements ApplicationListener<ContextRefreshedEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(StartEventListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        if (ctx != null) {
            RedisQueue redisQueue = ctx.getBean(RedisQueueImpl.class);
            String  regEnable  = AppEnvContext.getProperty(Constants.MYKIT_DELAY_REGISTRY_ENABLE, "false");
            if (!redisQueue.isRunning() && !Boolean.parseBoolean(regEnable)) {
                LOGGER.info("starting Queue StandAlone Model ...");
                redisQueue.start();
            }
        }
    }
}
