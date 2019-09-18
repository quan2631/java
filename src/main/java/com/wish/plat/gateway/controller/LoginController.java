package com.wish.plat.gateway.controller;

import com.wish.plat.common.PlatData;
import com.wish.plat.common.PlatRequestBody;
import com.wish.plat.common.PlatResponseBody;
import com.wish.plat.gateway.tokenStore.service.TokenService;
import com.wish.plat.util.token.JwtTokenUtil;
import com.wish.plat.util.token.JwtUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author: QUAN
 * @date: Created in 2019/9/5 14:35
 * @description: 鉴权对外提供接口
 * @modified By:
 */
@RestController
@RequestMapping("api")
public class LoginController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("login")
    public PlatResponseBody login(@RequestBody PlatRequestBody requestBody){
        /**
         * 1、得到所需信息
         */
        PlatData data = requestBody.getData();
        Map<String, Map<String, Object>> params = data.getPlat();
        Map<String, Object> loginMap = params.get("login");
        String loginId = loginMap.get("loginId").toString();
        String password = loginMap.get("password").toString();
        /**
         * 2、验证用户密码和返回token
         */
        String tokenId = this.getToken(loginId, password);
        /**
         *3、存储到redis
         */
        tokenService.saveToken(loginId, tokenId);
        /**
         *4、设置返回信息
         */
        PlatResponseBody ret = new PlatResponseBody();
        if(null == tokenId){
            ret.setCode("0");
            ret.setMsg("认证失败");
        }else{
            ret.setCode("1");
            ret.setMsg(tokenId);
        }
        return ret;
    }

    /**
     * 通过rpc方式验证用户登录并返回token
     * @return
     */
    private String getToken(String loginId,  String password){
        /**
         * 1、rpc验证用户密码，现在默认是通过
         */
        boolean allow = true;
        if(!allow){
            return  null;
        }
        String cityID = "043200";
        String userID = loginId; // 即JwtUserDetail 的 operCode。redis的key
        String userRoleCode= "1001";
        String userOrgCode = "100";
        String userChannel = "1";
        /**
         * 2、构建需要信息及生产token
         */
        JwtUserDetail user = new JwtUserDetail(cityID,userID,userRoleCode,userOrgCode,userChannel);
        JwtTokenUtil jwtTokenUtil = JwtTokenUtil.getInstance();
        return jwtTokenUtil.getToken(userID, user);
    }

    /**
     * 用户注销，移除token
     * @param loginId
     * @return
     */
    @GetMapping("removeToken/{loginId}")
    public void removeToken(@PathVariable("loginId") String loginId){
        tokenService.removeToken(loginId);
    }
}
