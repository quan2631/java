package com.wish.plat.gateway.routeStore.service;

import com.alibaba.fastjson.JSON;
import com.wish.plat.gateway.routeStore.repository.RedisRouteDefinitionRepository;
import com.wish.plat.gateway.routeStore.vo.GatewayRouteDefinition;
import com.wish.plat.gateway.vo.ResponseResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: QUAN
 * @date: Created in 2019/8/2 16:46
 * @description: 动态路由的service实现
 * @modified By:
 */
@ConditionalOnProperty(prefix = "gateway.store.redis", name = "enable", havingValue = "true")
@Service
public class DynamicRouteService  implements ApplicationEventPublisherAware {
    @Resource
    private RedisRouteDefinitionRepository routeDefinitionWriter;

    @Resource
    private RedisTemplate redisTemplate;

    private ApplicationEventPublisher publisher;

    // 通知刷新路由表
    private void notifyChanged() {
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    // 添加路由
    public ResponseResult add(GatewayRouteDefinition gatewayRouteDefinition){
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(gatewayRouteDefinition.getId());
        routeDefinition.setUri(
                UriComponentsBuilder.fromUriString(
                        gatewayRouteDefinition.getHostUrl()
                ).build().toUri()
        );
        routeDefinition.setPredicates(gatewayRouteDefinition.getPredicates());
        routeDefinition.setFilters(gatewayRouteDefinition.getFilters());
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
        notifyChanged();
        return new ResponseResult(HttpStatus.OK.value(), "路由创建成功", gatewayRouteDefinition);
    }

    // 删除路由
    public ResponseResult delete(String routeId) {
        routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
        /**
         * 删除也通知刷新
         * 否则页面会存在一条空记录（内存里），数据库没有
         */
        notifyChanged();
        return new ResponseResult(HttpStatus.OK.value(), "路由删除成功", routeId);
    }

    // 更新路由
    public ResponseResult update(GatewayRouteDefinition gatewayRouteDefinition) {
        this.delete(gatewayRouteDefinition.getId());
        this.add(gatewayRouteDefinition);
        return new ResponseResult(HttpStatus.OK.value(), "路由更新成功", gatewayRouteDefinition);
    }

    // 获取一条路由
    public Mono<GatewayRouteDefinition> getOne(String routeId){
        Mono<RouteDefinition> RouteDefinition = routeDefinitionWriter.findOne(Mono.just(routeId));
        return RouteDefinition.filter(r -> r!=null).flatMap(r -> {
            GatewayRouteDefinition gatewayRouteDefinition = new GatewayRouteDefinition();
            gatewayRouteDefinition.setId(routeId);
            gatewayRouteDefinition.setFilters(r.getFilters());
            gatewayRouteDefinition.setHostUrl(r.getUri().toString());
            gatewayRouteDefinition.setPredicates(r.getPredicates());
            return Mono.just(gatewayRouteDefinition);
        });
    }

    // 获取所有路由
    public Flux<RouteDefinition> getRoutes() {
        return routeDefinitionWriter.getRouteDefinitions();
    }

    // 删除所有路由
    public boolean delAll(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * 根据接口类名获取配置的直连地址
     * @param interfaceClass
     * @return
     */
    public static final String GATEWAY_ROUTES = "sofa_gateway_router";
    public String getDirectUrl(String interfaceClass){
        String directUrl = "";
        List<RouteDefinition> routeDefinitions = routeDefinitionWriter.getRouteDefinitionsList();
        for (RouteDefinition one: routeDefinitions){
            String interfaceName = one.getFilters().get(0).getArgs().get("_genkey_4");
            if(interfaceClass.equals(interfaceName)){
                directUrl= one.getUri().toString();
                break;
            }
        }
        if("".equals(directUrl)){
            throw new RuntimeException("Unable to get direct address. QX");
        }
        return directUrl;
    }
}
