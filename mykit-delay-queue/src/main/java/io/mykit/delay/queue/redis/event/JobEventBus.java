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
package io.mykit.delay.queue.redis.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Job事件业务
 */
public class JobEventBus {
    private EventBus      bus      = null;
    private AtomicBoolean register = new AtomicBoolean(false);

    private JobEventBus() {
        bus = new AsyncEventBus(MoreExecutors.newDirectExecutorService());
    }

    public static JobEventBus getInstance() {
        return LazyHolder.JEB;
    }

    public void register(JobEventListener listener) {
        if (register.compareAndSet(false, true)) {
            bus.register(listener);
        }

    }

    public void unregister(JobEventListener listener) {
        if (register.get() == true) {
            bus.unregister(listener);
        }
    }

    public void post(JobEvent event) {
        if (register.get() == true) {
            bus.post(event);
        }

    }

    private static class LazyHolder {

        private static final JobEventBus JEB = new JobEventBus();
    }
}
