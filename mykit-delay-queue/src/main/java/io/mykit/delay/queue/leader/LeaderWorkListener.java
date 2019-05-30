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

import io.mykit.delay.queue.core.Queue;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description Leader工作流监听器
 */
public class LeaderWorkListener implements LeaderLatchListener {
    public static final Logger LOGGER = LoggerFactory.getLogger(LeaderWorkListener.class);
    private Queue queue;

    @Override
    public void isLeader() {
        LOGGER.warn("is starting work!");
        queue.start();
    }

    @Override
    public void notLeader() {
        LOGGER.warn("is stoping work!");
        queue.stop();
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }
}
