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

import com.google.common.collect.Lists;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/30
 * @description Redis支持类
 */
public class RedisSupport {
    private StringRedisTemplate template;

    public StringRedisTemplate getTemplate() {
        return template;
    }

    public void setTemplate(StringRedisTemplate template) {
        this.template = template;
    }

    public void deleteKey(String... key) {
        this.template.delete(Lists.newArrayList(key));
    }

    public void set(String k, String v) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        ops.set(k, v);
    }

    public void set(String k, String v, long var3, TimeUnit var5) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        ops.set(k, v, var3, var5);
    }

    public String get(String key) {
        ValueOperations<String, String> ops = this.template.opsForValue();
        return ops.get(key);
    }

    public void leftPush(String key, String item) {
        ListOperations<String, String> listOperations = template.opsForList();
        listOperations.leftPush(key, item);
    }

    public List<String> lrange(String key, int start, int size) {
        ListOperations<String, String> listOperations = template.opsForList();
        return listOperations.range(key, start, size);
    }

    public boolean lrem(String key, String value) {
        try {
            ListOperations<String, String> listOperations = template.opsForList();
            listOperations.remove(key, 1, value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String leftPop(String key) {
        ListOperations<String, String> listOperations = template.opsForList();
        return listOperations.leftPop(key);
    }

    public String rightPop(String key) {
        ListOperations<String, String> listOperations = template.opsForList();
        return listOperations.rightPop(key);
    }

    public void rightPush(String key, String item) {
        ListOperations<String, String> listOperations = template.opsForList();
        listOperations.rightPush(key, item);
    }

    public void hashPutAll(String key, Map<String, String> map) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        hashOperations.putAll(key, map);
    }

    public void hashPut(String key, String hashKey, String hashValue) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        hashOperations.put(key, hashKey, hashValue);
    }

    public String getHashKey(String key, String mapKey) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        return hashOperations.get(key, mapKey);
    }

    public Set<String> getHashKeys(String key) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        return hashOperations.keys(key);
    }

    /**
     * 删除hash中的指定key
     */
    public void deleteHashKeys(String key, Object... keys) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        hashOperations.delete(key, keys);
    }

    public List<String> getHashKeys(String key, Collection<String> keys) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        return hashOperations.multiGet(key, keys);
    }

    public List<String> hashgetAll(String key) {
        HashOperations<String, String, String> hashOperations = template.opsForHash();
        List<String>                           lst            = hashOperations.values(key);
        return lst;
    } //zset.

    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        ZSetOperations<String, String> zset = template.opsForZSet();

        Set<String> datas = zset.rangeByScore(key, min, max, offset, count);
        return datas;
    }

    public Set<ZSetOperations.TypedTuple<String>> zrangeByScoreWithScores(String key, double min, double max, int offset,
                                                                          int count) {
        ZSetOperations<String, String>         zset = template.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> set  = zset.rangeByScoreWithScores(key, min, max, offset, count);
        return set;
    }

    public boolean zadd(String key, String itemKey, double score) {
        ZSetOperations<String, String> zset = template.opsForZSet();
        return zset.add(key, itemKey, score);
    }

    public Long zrem(String key, String itemKey) {
        ZSetOperations<String, String> zset = template.opsForZSet();
        return zset.remove(key, itemKey);
    }

    public Boolean setNx(final String key, final String value) {
        final org.springframework.data.redis.serializer.RedisSerializer redisSerializer      = template.getKeySerializer();
        final org.springframework.data.redis.serializer.RedisSerializer redisValueSerializer = template.getValueSerializer();
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.setNX(redisSerializer.serialize(key), redisValueSerializer.serialize(value));
            }
        });
    }

    public Boolean pExpire(final String key, final long timeout) {
        final org.springframework.data.redis.serializer.RedisSerializer redisSerializer = template.getKeySerializer();
        return template.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.pExpire(redisSerializer.serialize(key), timeout);
            }
        });
    }

    /**
     * command GETSET key value
     */
    public String getSet(final String key, final String value) {
        final org.springframework.data.redis.serializer.RedisSerializer redisSerializer      = template.getKeySerializer();
        final org.springframework.data.redis.serializer.RedisSerializer redisValueSerializer = template.getValueSerializer();
        return template.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] b = connection.getSet(redisSerializer.serialize(key), redisValueSerializer.serialize(value));
                return template.getStringSerializer().deserialize(b);
            }
        });
    }

    public Jedis getJedis() {
        return (Jedis) template.getConnectionFactory().getConnection().getNativeConnection();
    }
}
