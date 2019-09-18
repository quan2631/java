package com.wish.plat.gateway.tokenStore.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author: QUAN
 * @date: Created in 2019/9/5 15:19
 * @description:
 * @modified By:
 */
@Service
public class TokenService {

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 私有保存方法
     * @param loginId
     * @param token
     */
    public void saveToken(String loginId, String token){
        redisTemplate.opsForValue().set(loginId, token);
        // 设置key值有效期
        this.refreshToken(loginId);
    }

    /**
     * 根据key移除token（注销时使用）
     * @param loginId
     */
    public void removeToken(String loginId){
        redisTemplate.delete(loginId);
    }

    /**
     * 根据key刷新token的有效时间
     * @param loginId
     */
    private void refreshToken(String loginId){
        /**
         * 重新设置key值有效期为300分钟
         */
        redisTemplate.expire(loginId, 300, TimeUnit.MINUTES);
    }
}
