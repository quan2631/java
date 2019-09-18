package com.wish.plat.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author: QUAN
 * @date: Created in 2019/9/16 15:48
 * @description: 优先级：第一。作用：设置Attributes，放入post请求体的内容
 * @modified By:
 */
@Slf4j
@Component
public class CacheRequestBodyFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 将 request body 中的内容 copy 一份，记录到 exchange 的一个自定义属性中
        Object cachedRequestBodyObject = exchange.getAttributeOrDefault(FilterConstant.CACHED_REQUEST_BODY_OBJECT_KEY, null);
        // 如果已经缓存过，略过
        if (cachedRequestBodyObject != null) {
            return chain.filter(exchange);
        }
        // 如果没有缓存过，获取字节数组存入 exchange 的自定义属性中
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                }).defaultIfEmpty(new byte[0])
                .doOnNext(bytes -> exchange.getAttributes().put(FilterConstant.CACHED_REQUEST_BODY_OBJECT_KEY, bytes))
                .then(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
