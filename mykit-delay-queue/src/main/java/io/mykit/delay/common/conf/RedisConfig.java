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
package io.mykit.delay.common.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.SerializationUtils;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2019/5/29
 * @description Redis配置
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory factory) {
        final RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new RedisSerializer() {
            @Override
            public byte[] serialize(Object o) throws SerializationException {
                return SerializationUtils.serialize(o);
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                return SerializationUtils.deserialize(bytes);
            }
        });
        //template.setHashValueSerializer( new GenericToStringSerializer< Object >( Object.class ) );
        template.setValueSerializer(new RedisSerializer() {
            @Override
            public byte[] serialize(Object o) throws SerializationException {
                return SerializationUtils.serialize(o);
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                return SerializationUtils.deserialize(bytes);
            }
        });
        return template;
    }
}
