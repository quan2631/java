package com.wish.plat.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @author: QUAN
 * @date: Created in 2019/8/1 14:27
 * @description: 基于IP的限流配置类
 * @modified By:
 */
@Configuration
public class RateLimiterConfig {
    @Bean(value = "remoteAddKeyResolver")
    public KeyResolver remoteAddKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}
