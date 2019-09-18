package com.wish.plat.gateway.config;

import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: QUAN
 * @date: Created in 2019/8/2 14:21
 * @description: 封装sofa配置类，一个应用信息、一个注册中心
 * 1、暂时没用，参考袁绍鹏的代码。sofa代理时设置，为了规范
 * 2、可以设置注册中心，后续可能会用到
 * @modified By:
 */
@Configuration
public class SofaConfig {

    // 应用名称
    @Value("${spring.application.name}")
    private String appName;

    // 应用名称
    @Value("${app.config.protocolType}")
    private String protocol;

    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig appConfiguration = new ApplicationConfig();
        appConfiguration.setAppName(appName);
        return appConfiguration;
    }
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol(protocol);
        return registryConfig;
    }
}
