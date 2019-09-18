package com.wish.plat.gateway.routeStore.controller;

import com.wish.plat.gateway.routeStore.service.DynamicRouteService;
import com.wish.plat.gateway.routeStore.vo.GatewayRouteDefinition;
import com.wish.plat.gateway.vo.ResponseResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: QUAN
 * @date: Created in 2019/8/2 16:39
 * @description: 前端请求入口
 * @modified By:
 */
@RestController
@RequestMapping("/plat-gateway")
public class GatewayRouteController {
    @Resource
    private DynamicRouteService routeService;

    @Resource
    private RouteDefinitionLocator routeDefinitionLocator;

    @Resource
    private RouteLocator routeLocator;
    @PostMapping("/routes")
    public Mono<ResponseResult> add(@RequestBody GatewayRouteDefinition gatewayRouteDefinition) {
        return Mono.just(routeService.add(gatewayRouteDefinition));
    }

    @GetMapping("/routes")
    public Mono<List<Map<String, Object>>> getAllRoutes() {
        Mono<Map<String, RouteDefinition>> routeDefs = this.routeDefinitionLocator.getRouteDefinitions()
                .collectMap(RouteDefinition::getId);
        Mono<List<Route>> routes = this.routeLocator.getRoutes().collectList();
        return Mono.zip(routeDefs, routes).map(tuple -> {
            Map<String, RouteDefinition> defs = tuple.getT1();
            List<Route> routeList = tuple.getT2();
            List<Map<String, Object>> allRoutes = new ArrayList<>();
            routeList.forEach(route -> {
                HashMap<String, Object> r = new HashMap<>();
                r.put("route_id", route.getId());
                r.put("order", route.getOrder());
                if (defs.containsKey(route.getId())) {
                    r.put("route_definition", defs.get(route.getId()));
                } else {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("predicate", route.getPredicate().toString());
                    if (!route.getFilters().isEmpty()) {
                        ArrayList<String> filters = new ArrayList<>();
                        for (GatewayFilter filter : route.getFilters()) {
                            filters.add(filter.toString());
                        }
                        obj.put("filters", filters);
                    }
                    if (!obj.isEmpty()) {
                        r.put("route_object", obj);
                    }
                }
                allRoutes.add(r);
            });
            return allRoutes;
        });
    }

    @DeleteMapping("/routes/{routeId}")
    public Mono<ResponseResult> delete(@PathVariable(name = "routeId") String routeId) {
        return Mono.just(routeService.delete(routeId));
    }

    @PostMapping(value = "/routes/{routeId}")
    public Mono<ResponseResult> update(@PathVariable(name = "routeId") String routeId,
                                       @RequestBody GatewayRouteDefinition gatewayRouteDefinition) {

        return routeService.getOne(routeId)
                .flatMap(route -> {
                    route.setFilters(gatewayRouteDefinition.getFilters());
                    route.setPredicates(gatewayRouteDefinition.getPredicates());
                    route.setHostUrl(gatewayRouteDefinition.getHostUrl());
                    return Mono.just(routeService.update(route));
                })
                .map(updateRoute -> new ResponseResult(HttpStatus.OK.value(),"更新完成",routeId))
                .defaultIfEmpty(new ResponseResult(HttpStatus.NOT_FOUND.value(), "更新失败", routeId));
    }

    @PostMapping(value = "/routes/cleanCache")
    public boolean cleanCache(@RequestParam String key) {
        return routeService.delAll(key);
    }

    @GetMapping("/getDirectUrl/{interfaceClass}")
    public String getDirectUrl(@PathVariable(name = "interfaceClass") String interfaceClass) {
        return routeService.getDirectUrl(interfaceClass);
    }
}
