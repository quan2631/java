package com.wish.plat.gateway.adapt.sofa;

import com.alipay.sofa.rpc.api.GenericService;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: QUAN
 * @date: Created in 2019/8/2 14:07
 * @description: 增加代理的缓存类（参考袁绍鹏的代码）
 * 1发现服务调用三次后会失败，2提高访问效率
 * remark :20191012
 * 此缓存类可以用一个静态map代替
 * 问题：目前配置的是一天后会清理缓存，这样周一调用，晚上清楚。周二、周三，都没问题
 * 周四调用，却出现出现三次的异常
 * 原因：不能设置为每天清理一次或者替代掉该方案
 * 此次先注释掉 ： .expireAfterWrite(1, TimeUnit.DAYS)
 * @modified By:
 */
public class SofaServiceCache {
    private static final Logger logger = LoggerFactory.getLogger(SofaServiceCache.class);
    /**
     * google guava cache对象
     */
    private static Cache<String, GenericService> sofaServiceCache =
            CacheBuilder.newBuilder()
                    .concurrencyLevel(8)
                    // .expireAfterWrite(1, TimeUnit.DAYS)
                    .initialCapacity(50)
                    .maximumSize(300)
                    .recordStats()
                    .removalListener((notification) -> {

                    }).build();

    // 获取服务, 传递的参数可以封装成bean
    public static GenericService getService(String interfaceClass, String directUrl,
                                            ApplicationConfig applicationConfig,
                                            RegistryConfig registryConfig
    ) {
        GenericService genericService = null;
        try{
            genericService = sofaServiceCache.get(interfaceClass, () -> {
                logger.info("服务: {} 没有在缓存中发现，开始创建", interfaceClass);
                ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>()
                        .setInterfaceId(interfaceClass)
                        .setApplication(applicationConfig)
                        .setProtocol("bolt") // 指定协议
                        .setDirectUrl(directUrl) // 动态指定直连地址
                        .setGeneric(true) //设置泛化调用
                        .setTimeout(10000);
                GenericService s = consumerConfig.refer();
                return s;
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return genericService;
    }
}
