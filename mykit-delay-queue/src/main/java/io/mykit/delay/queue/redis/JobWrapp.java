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
package io.mykit.delay.queue.redis;

import io.mykit.delay.queue.JobMsg;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 任务包装类
 */
public class JobWrapp extends JobMsg {

    private static final long serialVersionUID = -4623468942451592116L;
    private String buckedName;

    public String getBuckedName() {
        return buckedName;
    }

    public void setBuckedName(String buckedName) {
        this.buckedName = buckedName;
    }
}
