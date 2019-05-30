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
package io.mykit.delay.common.utils;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description 状态枚举类
 */
public enum Status {
    WaitPut,//待加入
    Delay,//已经进入延时队列
    Ready,//已经出了延时队列 客户端可以方法此数据
    Finish,//客户端已经处理完数据了
    Delete,//客户端已经把数据删除了
    Restore,//手动恢复重发状态/或者是在实时队列中验证时间出现异常 再次放入buck中
    ConsumerFailRestore//消费失败
}
