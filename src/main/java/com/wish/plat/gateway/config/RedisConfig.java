package com.wish.plat.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author: QUAN
 * @date: Created in 2019/8/6 8:43
 * @description: 增加此配置类，替默认的
 * redis默认使用的jdk的
 * @modified By:
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();

        //使用fast json序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // value值的序列化采用 StringRedisSerializer
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        // key的序列化采用StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}
