package com.wish.plat.gateway.routeStore.repository;

import com.alibaba.fastjson.JSON;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: QUAN
 * @date: Created in 2019/8/2 16:49
 * @description: RouteDefinitionRepository 的redis实现
 * @modified By:
 */
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    public static final String GATEWAY_ROUTES = "gateway_router";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        redisTemplate.opsForHash().values(GATEWAY_ROUTES).stream()
                .forEach(routeDefinition -> routeDefinitions.add(
                        JSON.parseObject(routeDefinition.toString(), RouteDefinition.class)
                ));
        return Flux.fromIterable(routeDefinitions);
    }

    public List<RouteDefinition> getRouteDefinitionsList() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        redisTemplate.opsForHash().values(GATEWAY_ROUTES).stream()
                .forEach(routeDefinition -> routeDefinitions.add(
                        JSON.parseObject(routeDefinition.toString(), RouteDefinition.class)
                ));
        return routeDefinitions;
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            redisTemplate.opsForHash().put(GATEWAY_ROUTES, routeDefinition.getId(), JSON.toJSONString(routeDefinition));
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (redisTemplate.opsForHash().hasKey(GATEWAY_ROUTES, id)) {
                redisTemplate.opsForHash().delete(GATEWAY_ROUTES, id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
        });
    }

    public Mono<RouteDefinition> findOne(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (redisTemplate.opsForHash().hasKey(GATEWAY_ROUTES, id)){
                String routeDefinitionJson = (String)redisTemplate.opsForHash().get(GATEWAY_ROUTES, id);
                RouteDefinition routeDefinition = JSON.parseObject(routeDefinitionJson, RouteDefinition.class);
                return Mono.justOrEmpty(routeDefinition);
            }else {
                return Mono.empty();
            }
        });
    }
}
