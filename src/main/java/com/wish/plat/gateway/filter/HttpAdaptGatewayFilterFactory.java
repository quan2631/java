package com.wish.plat.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * @author: QUAN
 * @date: Created in 2019/9/3 13:57
 * @description: Http的协议转化过滤器
 * @modified By:
 */
@Component
public class HttpAdaptGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            System.out.println("http filter is here");
            return chain.filter(exchange);
        };
    }
}
