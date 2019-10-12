package com.wish.plat.gateway.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bifeng
 * @version 2019-06-29
 * @modified By: QUAN 2019-09-10
 * 1、去掉log，可以方便执行main方法
 * 2、调整方法名，由 createJWT 变更为 getToken
 * 3、调整 getToken 入参，去掉主题，入参为key和需要的对象
 * 4、主题一般设置为登录名，去掉该入参
 * 5、去掉有效期的使用，在网关采用redis做有效期
 * 6、增加单例
 */
public class JwtTokenUtil {
    /**
     * 获取token中用户ID等信息,返回token中含有的用户相关信息对象
     * @param token
     * @return UserTokenInfo
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Wish ";
    private static final String CLAIM_KEY_CITY_ID = "city_id";
    private static final String CLAIM_KEY_USER_ID = "user_id";
    private static final String CLAIM_KEY_OPER_CODE = "oper_code";
    private static final String CLAIM_KEY_USER_ROLE_CODE = "user_role_code";
    private static final String CLAIM_KEY_USER_ORG_CODE = "user_org_code";
    private static final String CLAIM_KEY_USER_CHANNEL = "user_channel";
    private static final String SECRET = "yondwishjwtsecret";
    private static final String ISS = "wish-plat";
    private static final String SESSIONID = "sessionId";

    /**
     * 获取单例
     */
    private JwtTokenUtil(){};
    public static JwtTokenUtil getInstance(){
       return getSingleInstance.single;
    }
    private static class getSingleInstance{
        private static JwtTokenUtil single = new JwtTokenUtil();;
    }
    /**
     * 获取token
     * @param id
     * @param user
     * @return
     * @throws Exception
     */
    public String getToken(String id, JwtUserDetail user){
        //指定签名的时候使用的签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        //创建payload的私有声明
        Map<String,Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_CITY_ID, user.getCityId());
        claims.put(CLAIM_KEY_USER_ID, user.getUserId());
        claims.put(CLAIM_KEY_OPER_CODE, user.getOperCode());
        claims.put(CLAIM_KEY_USER_ROLE_CODE, user.getOperRoleCode());
        claims.put(CLAIM_KEY_USER_ORG_CODE, user.getOperOrgCode());
        claims.put(CLAIM_KEY_USER_CHANNEL, user.getOperChannel());
        claims.put(SESSIONID, user.getSessionId());

        //生成签名的时候使用的秘钥secret
        SecretKey key = generalKey();

        //为payload添加各种标准声明和私有声明
        JwtBuilder builder = Jwts.builder()
                                .setClaims(claims)
                                .setId(id)// 设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                                .setIssuedAt(now)// iat: jwt的签发时间
                                .setIssuer(ISS)// issuer：jwt签发人
                                .signWith(signatureAlgorithm,key);
        return builder.compact();
    }

    /**
     * 根据token获取用户
     * @param token
     * @return
     */
    public static JwtUserDetail getUserInfoByToken(String token){
        JwtUserDetail jwtUserDetail;
        try {
             Claims claims = getClaimsFromToken(token);
            String cityID = claims.get(CLAIM_KEY_CITY_ID)==null?"":claims.get(CLAIM_KEY_CITY_ID).toString();
            String userID =  claims.get(CLAIM_KEY_USER_ID)==null?"":claims.get(CLAIM_KEY_USER_ID).toString();
            String operCode =  claims.get(CLAIM_KEY_OPER_CODE)==null?"":claims.get(CLAIM_KEY_OPER_CODE).toString();
            String userRoleCode = claims.get(CLAIM_KEY_USER_ROLE_CODE)==null?"":claims.get(CLAIM_KEY_USER_ROLE_CODE).toString();
            String userOrgCode = claims.get(CLAIM_KEY_USER_ORG_CODE)==null?"":claims.get(CLAIM_KEY_USER_ORG_CODE).toString();
            String userChannel = claims.get(CLAIM_KEY_USER_CHANNEL)==null?"":claims.get(CLAIM_KEY_USER_CHANNEL).toString();
            String sessionid = claims.get(SESSIONID)==null?"":claims.get(SESSIONID).toString();
            jwtUserDetail = new JwtUserDetail(cityID,userID,operCode,userRoleCode,userChannel,userOrgCode,sessionid);
        } catch (Exception e) {
            //  log.error("error:", e.getMessage());
            jwtUserDetail = null;
        }
        return jwtUserDetail;
    }
    public static  Claims getClaimsFromToken(String token){
        Claims claims;
        try {
        	
        		  claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        	
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    /**
     * 由字符串生成加密key
     * @return
     */
    private SecretKey generalKey() {
        // 本地的密码解码
    	  byte[] encodedKey = Base64.decodeBase64(SECRET);;

        // 根据给定的字节数组使用AES加密算法构造一个密钥
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }
    public static void main(String[] args) throws Exception {
        String cityID = "043200";
        String userID = "admin";
        String opercode="aaaaaa";
        String userRoleCode= "1001";
        String userOrgCode = "100";
        String userChannel = "1";
        JwtUserDetail user = new JwtUserDetail(cityID,userID,opercode,userRoleCode,userChannel,userOrgCode,"aaaaaaaaaa");
        JwtTokenUtil util = JwtTokenUtil.getInstance();
       System.out.println(JwtTokenUtil.getUserInfoByToken("eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX29yZ19jb2RlIjoiMDciLCJ1c2VyX2NoYW5uZWwiOiJIWDAyIiwidXNlcl9pZCI6InUwMDEiLCJ1c2VyX3JvbGVfY29kZSI6IiIsImlzcyI6Indpc2gtcGxhdCIsInNlc3Npb25JZCI6ImMyODQ4ZTMwLTJiM2QtNGM3Zi05MDRkLTBjNTU1NDUwMTFhZCIsImlhdCI6MTU2ODI2ODE4OCwianRpIjoidTAwMSIsImNpdHlfaWQiOiIwNDMyMDAifQ.hfwHTBnpkkwhTMygou24C1nqfiEJr7VNaXhOwO9OH7I").getOperCode());
        System.out.println("token is："+util.getToken("zhangsan", user));
    }
}
