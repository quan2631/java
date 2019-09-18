package com.wish.plat.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: QUAN
 * @date: Created in 2019/7/25 15:56
 * @description: 服务不可达和下游服务超时的情况下
 * Spring Cloud Gateway成功进行了熔断。
 * @modified By:
 */
@RestController
public class SelfHystrixController {
    @RequestMapping("/defaultfallback")
    public Map<String,String> defaultfallback(){
        System.out.println("请求被熔断.");
        Map<String,String> map = new HashMap<>();
        map.put("Code","fail");
        map.put("Message","服务异常");
        map.put("result","");
        return map;
    }
}
