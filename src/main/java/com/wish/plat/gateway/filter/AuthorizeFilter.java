package com.wish.plat.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.wish.plat.common.PlatData;
import com.wish.plat.common.PlatRequestBody;
import com.wish.plat.gateway.util.JwtTokenUtil;
import com.wish.plat.gateway.util.JwtUserDetail;
import com.wish.plat.gateway.util.UniqRequestIdGen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: QUAN
 * @date: Created in 2019/7/25 14:42
 * @description: Token 全局过滤器，场景： token存放在redis中
 * 优先级：第三。作用：对请求进行token认证，并解析token
 * @modified By:
 */
@Component
@Slf4j
public class AuthorizeFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZE_TOKEN = "token";

    /**
     * 是否需要Token认证
     */
    @Value("${app.config.authFlag}")
    private boolean authFlag;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Object cachedRequestBodyObject = exchange.getAttributeOrDefault(FilterConstant.CACHED_REQUEST_BODY_OBJECT_KEY, null);

        // 如果不需要auth认证则直接返回
        if(!authFlag) return chain.filter(exchange);

        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        HttpHeaders headers = serverHttpRequest.getHeaders();
        String token = headers.getFirst(AUTHORIZE_TOKEN);
        if (token == null) {
            token = serverHttpRequest.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }
        /**
         * 1、利用token获取key
         */
        JwtTokenUtil jwtTokenUtil = JwtTokenUtil.getInstance();
        JwtUserDetail user = jwtTokenUtil.getUserInfoByToken(token);
        String uid = user.getOperCode();

        /**
         * 2、从redis中取出与请求的token参数比较
         * A如果不通过，直接返回
         */
        ServerHttpResponse response = exchange.getResponse();
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(uid)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String authToken = stringRedisTemplate.opsForValue().get(uid);
        if (authToken == null || !authToken.equals(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        /**
         * 3、如果通过后，延长存活时间
         */
        stringRedisTemplate.expire(uid, 300, TimeUnit.MINUTES);
        System.out.println(UniqRequestIdGen.gettracerId());
        /**
         * 4、将解析的结果放入到请求参数中，并继续传递
         * 4.1 获取post 体内容
         */
        byte[] body = (byte[]) cachedRequestBodyObject;
        String bodyString = new String(body);
        log.warn("request body:");
        log.warn(bodyString);
        /**
         * 4.2 将post 体翻译为对象，解析token，并设置userInfo
         */
        PlatRequestBody requestBody = JSON.parseObject(bodyString,PlatRequestBody.class );
        PlatData date = requestBody.getData();
        Map<String, Map<String, Object>> plat = date.getPlat();
        Map<String, Object> userInfoMap = (Map<String, Object>)JSON.toJSON(user);
        plat.put("userInfo", userInfoMap);
        /**
         * 4.3 将追加的userInfo 转换为字节流
         */
        String newBodyStr = JSON.toJSONString(requestBody);
        byte[] newBodyStrBytes = newBodyStr.getBytes();
        /**
         * 4.4 将字节流放入一个新request 中，并继续流转。（未完成）
         * （注意：此处应给设置为 newBodyStrBytes，但是无效。因此下边代码还继续使用 body）
         */
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                if (body.length > 0) {
                    return Flux.just(dataBufferFactory.wrap(body));
                }
                return Flux.empty();
            }
        };
        return chain.filter(exchange.mutate().request(decorator).build());
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
