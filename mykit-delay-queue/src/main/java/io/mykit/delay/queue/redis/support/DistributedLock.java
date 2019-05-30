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

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description 分布式锁
 */
public interface DistributedLock {

    /**
     * 尝试加锁
     * @param key 加锁的key
     * @return 尝试加锁成功返回true; 失败返回false
     */
    boolean tryLock(String key);

    /**
     * 尝试加锁
     * @param key 加锁的key
     * @param timeout 超时时间
     * @return 尝试加锁成功返回true; 失败返回false
     */
    boolean tryLock(String key, long timeout);

    /**
     * 加锁操作
     * @param key  加锁的key
     * @return 加锁成功返回true; 失败返回false
     */
    boolean lock(String key);
    /**
     * 解锁操作
     * @param key  解锁的key
     * @return 解锁成功返回true; 失败返回false
     */
    void unlock(String key);
}
