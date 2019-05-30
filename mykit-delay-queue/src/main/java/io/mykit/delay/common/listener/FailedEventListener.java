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

import io.mykit.delay.queue.redis.RedisQueue;
import io.mykit.delay.queue.redis.RedisQueueImpl;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 失败事件监听器
 */
@Configuration
public class FailedEventListener implements ApplicationListener<ApplicationFailedEvent>  {
    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        Throwable throwable = event.getException();
        handler(throwable, event);
    }

    private void handler(Throwable throwable, ApplicationFailedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        if (ctx != null) {
            RedisQueue redisQueue = ctx.getBean(RedisQueueImpl.class);
            if (redisQueue != null && redisQueue.isRunning()) {
                redisQueue.stop();
            }
        }
    }
}
