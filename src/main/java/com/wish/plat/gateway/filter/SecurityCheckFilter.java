package com.wish.plat.gateway.filter;

import com.alipay.sofa.common.utils.StringUtil;
import com.wish.plat.gateway.config.SecurityCheckConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author: QUAN
 * @date: Created in 2019/8/19 11:20
 * @description: 安全校验过滤器类
 * 优先级：第二。作用：对提交的参数值和服务器地址进行安全检查
 * @modified By:
 */
@Component
@Slf4j
public class SecurityCheckFilter implements GlobalFilter, Ordered {

    @Autowired
    private SecurityCheckConfig securityCheckConfig;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 如果开启了安全检查的开关
        if(securityCheckConfig.isCheck()){
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = request.getHeaders();

            // 如果开启了header检查
            if(securityCheckConfig.isCheckHeader()){
                // 组装header信息
                StringBuffer sb = new StringBuffer(" ");
                for(String key: headers.keySet()){
                    if(!securityCheckConfig.getHeaderWhiteName().contains(key)){
                        sb.append(headers.getFirst(key) + " ");
                    }
                }
                // 如果符合配置的规则
                if(securityCheckConfig.matches(sb.toString())){
                    log.info("[securityErr] header not allow" );
                    response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                    return setReturnMsg("[securityErr] header not allow", response);
                }
            }

            // 针对host的篡改
            if(securityCheckConfig.isCheckHost()){
                String hostValue = headers.getFirst("host");
                if(!securityCheckConfig.getHostWhiteName().contains(hostValue)){
                    log.info("[securityErr] host not allow" );
                    response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                    return setReturnMsg("[securityErr] host not allow", response);
                }
            }

            // 针对referer的跨站点请求伪造
            if(securityCheckConfig.isCheckReferer()){
                String refererValue = headers.getFirst("referer");
                if(StringUtil.isNotBlank(refererValue)){
                    if(!securityCheckConfig.getRefererWhiteName().contains(refererValue)){
                        log.info("[securityErr] referer not allow" );
                        response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                        return setReturnMsg("[securityErr] referer not allow", response);
                    }
                }
            }

            // 拦截post的参数
            if(securityCheckConfig.isCheckParameter()){
                Object cachedRequestBodyObject = exchange.getAttributeOrDefault(FilterConstant.CACHED_REQUEST_BODY_OBJECT_KEY, null);
                byte[] body = (byte[]) cachedRequestBodyObject;
                String paramsStr = new String(body);
                if(securityCheckConfig.matches(paramsStr)){
                    log.info("[securityErr] parameter not allow" );
                    response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
                    return response.writeWith(Flux.just(response.bufferFactory().wrap("[securityErr] parameter not allow".getBytes())));
                }
            }
        }
        return chain.filter(exchange);
    }

    /**
     * 设置提示信息返回
     * @param msg
     * @param response
     * @return
     */
    private Mono<Void>  setReturnMsg(String msg,  ServerHttpResponse response){
        return response.writeWith(Flux.just(response.bufferFactory().wrap(msg.getBytes())));
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
