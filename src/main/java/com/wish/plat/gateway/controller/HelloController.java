package com.wish.plat.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 进行一些简单的测试、验证，后续可以删除
 */
@RestController
public class HelloController {
    /**
     * 验证熔断机制
     * 配置的熔断策略是3秒，调用此方法会超时
     * @return
     */
    @RequestMapping("/timeout")
    public String timeout(){
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "hello, I am gateway service.";
    }
}
