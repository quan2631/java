<!-- TOC --> 
[1. 协议转换](#1-协议转换)

[2. 鉴权认证](#2-鉴权认证)

[3. 熔断限流](#3-熔断限流)

[4. 安全检查](#4-安全检查)

[5. 日志](#5-日志)

[6. 其他](#6-其他)

<!-- /TOC -->
# 1. 协议转换
## 1.1. 配置页，字段说明见添加页面
```
http://localhost:8000/index.html#/
```
注：目前页面支持http转http和http转sofa。
## 1.2. 示例一：调用plat-idgenerator发布的sofa接口
``` 
请求地址：http://127.0.0.1:8000/id/get
入参：
{
  "params": [
    {
      "key": "plat-base-compet"
    }
  ]
}
返回：
"1000000016"
``` 
注：Get请求不支持，入参与配置一致。
## 1.3. 示例二：调用plat-base发布的sofa接口

```
请求地址：http://127.0.0.1:8000/base/api/role/getRoleList
入参：
{
  "params": [
    {
      "city_id": "01001"
    }
  ]
}
返回：略
```
注：Get请求不支持，入参与配置一致。
## 1.4. 示例三：调用plat-base发布的sofa接口（对象作为入参）
```
请求地址：http://127.0.0.1:8000/local/sayHelloNestedObj
入参：
{
  "params": [
    {
      "student": {
		"name": "王大宝",
        "sex": "女",
        "school": {
          "name": "北京市第一小学",
          "address": "长江路28号"
        }
      }
    }
  ]
}
返回：
"hello Student(name=王大宝, sex=女, school=School(name=北京市第一小学, address=长江路28号))!"
```
注：目前只是简单示意，后续根据接口规范进行调整
## 1.5. 示例四：调用plat-base发布的http接口（对象作为入参）
```
请求地址：http://127.0.0.1:8000/hello/httpTest/sayHelloObj
入参：
{
  "name": "人大第一附属小学",
  "address": "北京市"
}
返回：
post sayHelloObjH:School(name=人大第一附属小学, address=北京市)!
```
## 1.6. 结论
通过在此页配置，可以实现不同服务间的转发。
# 2. 鉴权认证
## 2.1. 实现策略
1. 用户登录后，会颁发Token并存储在redis中
2. 每次请求必须有token信息且与颁发的一致才可以返回数据，否则直接返回401
## 2.2. Redis配置
```
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    #数据库索引
    database: 15
    #连接超时时间
    timeout: 5000
    lettuce:
      shutdown-timeout: 5000
      pool:
        #最小空闲
        min-idle: 0
        #最大阻塞等待时间(负数表示没限制)
        max-wait: -1
        #最大空闲
        max-idle: 8
        #最大连接数
        max-active: 8
```
## 2.3. 全局过滤器
```
com.wish.plat.gateway.filter.AuthorizeFilter
```
## 2.4. 如何验证
1. 打开认证开关
```
# 项目自定义参数 #
app:
  config:
    # 是否需要anth认证
    authFlag: true
```
2. 用户登录，颁发token
```
POST：http://127.0.0.1:8000/api/login
入参：
{
  "data": {
    "common": {
      "retimestamp": 1567593358964,
      "channelseq": "",
      "eventCode": "B70001",
      "businessCode": "B70001"
    },
    "plat": {
      "login": {
        "loginId": "zhangsan",
        "password": "123",
        "loginType": 0
      }
    }
  }
}
返回：
{
"data": null,
"code": "1",
"msg": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX29yZ19jb2RlIjoiMSIsInVzZXJfY2hhbm5lbCI6IjEwMCIsInVzZXJfaWQiOiJ6aGFuZ3NhbiIsInVzZXJfcm9sZV9jb2RlIjoiMTAwMSIsImlzcyI6Indpc2gtcGxhdCIsImlhdCI6MTU2ODEwMDYwNywianRpIjoiemhhbmdzYW4iLCJjaXR5X2lkIjoiMDQzMjAwIn0.vpf9OX3GWB6IscUs8fufNwuSgNXDhV5dDVJO9iy9BGg"
}
```
3. 发送有Token的请求
```
header配置
name:token value:eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJ6aGFuZ3NhbiJ9.2BQvQP4Suhk1uO-aAdhap4hM1KoyuZosFVTJZbpoH70

请求地址：http://127.0.0.1:8000/hello/httpTest/sayHelloObj
入参：
{
  "name": "人大第一附属小学",
  "address": "北京市"
}
返回：
post sayHelloObjH:School(name=人大第一附属小学, address=北京市)!
```
4. 有效期验证
```
1、目前默认配置100秒
2、持续发起请求，则会更新token的有效期。利用ttl zhangsan，可以看到token对应的有效期会持续更新
3、停止操作等待100秒，则返回401
```
# 3. 熔断限流
## 3.1. 定义
1. 熔断（也称熔断降级）：当外部请求经网关发起请求时，不可避免会存在失败的可能。此时不能让失败请求堆积的网关，需要尽快返回结果。因此，需要熔断、降级操作。
2. 限流：举例说明，一个自来水厂最多只够100人同时使用，当达到峰值后可以拒绝提供服务。这样既会保护水厂，又能提高100人的满意度。总之，就是提高可用性和稳定性。
3. 如上，纯属个人解释。

## 3.2. yml中熔断器和限流器配置
```
spring:
  cloud:
    # Gateway routes config
    gateway:
      #
      # default-filters:
      #  -
      routes:
        # 熔断与限流测试
        - id: errorTest
          #下游服务地址
          uri: http://127.0.0.1:8085/
          order: 0
          #网关断言匹配
          predicates:
            - Path=/gateway/**
          filters:
            #熔断过滤器
            - name: Hystrix
              args:
                name: fallbackcmd
                fallbackUri: forward:/defaultfallback
            - StripPrefix=1
              #限流过滤器
            - name: RequestRateLimiter
              args:
               key-resolver: '#{@remoteAddKeyResolver}'
               # 每秒最大访问次数（放令牌桶的速率）
               redis-rate-limiter.replenishRate: 20
               # 令牌桶最大容量（令牌桶的大小）
               redis-rate-limiter.burstCapacity: 20
#熔断器配置
hystrix:
  command:
    default:
      execution:
        isolation:
          strategy: SEMAPHORE
          thread:
            timeoutInMilliseconds: 3000
  shareSecurityContext: true
  #网关日志输出
logging:
  level:
    org.springframework.cloud.gateway: TRACE
    org.springframework.http.server.reactive: DEBUG
    org.springframework.web.reactive: DEBUG
    reactor.ipc.netty: DEBUG
```
注：
1. 超时异常熔断采用hystrix的SEMAPHORE策略，超时时间为3秒，如果下游服务不可达（异常），将由fallbackcmd处理，路由到本地http://127.0.0.1:8085/defaultfallback 处理。

## 3.3. 构建defaultfallback处理器
```
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
```
## 3.4. 本地构建测试服务
```
/**
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
/**
  * 正常的方法返回值
  * @return
  */
@RequestMapping("/hello")
public String hello(){
    return "hello world!" ;
}
```
## 3.5. 如何进行熔断测试
1. 正常请求
```
Get请求：
http://127.0.0.1:8085/gateway/hello
返回：
hello world!
```
2. 超时请求
```
Get请求：
http://127.0.0.1:8085/gateway/timeout
返回：
{
"result": "",
"Message": "服务异常",
"Code": "fail"
}
```
## 3.6. 限流策略
1. 理论学习，可参考
```
https://pan.baidu.com/s/1cogAzqn5AGTJ1QOjbP7CUA
```
2. 此处，我们选择基于redis令牌桶算法、基于IP的限流方式。

## 3.7. 添加限流键参数
1. yml中配置限流过滤器，此处省略（可参考熔断处配置）。
2. 基于IP的限流配置类
```
@Configuration
public class RateLimiterConfig {
    @Bean(value = "remoteAddKeyResolver")
    public KeyResolver remoteAddKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}
```
## 3.8. 如何进行限流测试
1. 选择JMeter进行压力测试

2. 配置1
```
# 每秒最大访问次数（放令牌桶的速率）
redis-rate-limiter.replenishRate: 2
# 令牌桶最大容量（令牌桶的大小）
redis-rate-limiter.burstCapacity: 10
```
结果：2秒内进行200次请求。异常率分别是：86%、89%、90%

3. 配置2
```
# 每秒最大访问次数（放令牌桶的速率）
redis-rate-limiter.replenishRate: 20
# 令牌桶最大容量（令牌桶的大小）
redis-rate-limiter.burstCapacity: 20
```
结果：2秒内进行200次请求。异常率分别是：34%、36%、47%

4. 详细测试结果，可参考
```
https://pan.baidu.com/s/1cogAzqn5AGTJ1QOjbP7CUA
```
# 4. 安全检查
## 4.1. 参考来源（老网厅相关-安全校验过滤器配置）
```
http://knowledge.pro.hxyd.tech:8080/pages/viewpage.action?pageId=10390153
```
## 4.2. 代码介绍
1. application-securityCheck.yml配置文件
```
security:
  check:
    #校验功能开关标志，true - 开，false - 关
    check: true
    #是否记录日志，true –记录日志，false –不记录日志
    log: true
    #是否检查request header信息，true –检查，false - 不检查
    checkHeader: true
    #是否检查request parameter信息，true –检查，false - 不检查
    checkParameter: true
    #是否检查request header中host信息，true –检查，false - 不检查
    checkHost: true
    #是否检查request header中referer信息，true –检查，false - 不检查
    checkReferer: true
    #示例规则第一类：匹配含有js脚本相关信息
    regex[0]: <script>.*</script>
    #示例规则第二类：匹配含有特殊字符信息
    regex[1]: '[''<>\#$\\^+|&~]'
    # POST提交会有双引号，因此其中的双引号
    #regex[1]: '[''<>\#$\\^+|&"~]'
    #示例规则第三类：防止目录穿越漏洞
    regex[2]: \\./|\\.\\./
    #示例规则第四类：匹配含有各种命令关键字信息
    keyWords[0]: alert
    keyWords[1]: select
    keyWords[2]: delete
    keyWords[3]: update
    keyWords[4]: insert
    keyWords[5]: exec
    keyWords[6]: drop
    keyWords[7]: count
    keyWords[8]: script
    keyWords[9]: join
    keyWords[10]: union
    keyWords[11]: truncate
    keyWords[12]: password
    keyWords[13]: and
    keyWords[14]: or
    keyWords[15]: cat
    #白名单
    headerWhiteName[0]: Accept
    headerWhiteName[1]: User-Agent
    parameterWhiteName[0]: tjbtn1
    parameterWhiteName[1]: _SAFETYINFOKEY
    hostWhiteName[0]: localhost:8000
    hostWhiteName[1]: 127.0.0.1:8000
    refererWhiteName[0]: localhost:8000
    refererWhiteName[1]: 127.0.0.1:8000
```
2. SecurityCheckConfig配置类，略。
3. SecurityCheckFilter过滤器，略。
## 4.3. 如何进行验证
1. post入参含有非法字符
```
http://127.0.0.1:8000/getRoleList
入参：
{
  "params": [
    {
      "city_id": "insert "
    }
  ]
}
返回（406 Not Acceptable）：
[securityErr] parameter not allow
```
2. Headers入参含有非法字符
```
Header name:toketId Header Value:123
正常返回数据

Header name:toketId Header Value:123#

返回（406 Not Acceptable）：
[securityErr] header not allow
```
# 5. 日志
## 5.1. 增加相关依赖
```
log4j2的省略
<!--log4j2 与kafka集成 -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
</dependency>
<!--异步日志使用-->
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <!--需要指定-->
    <version>3.4.2</version>
</dependency>
```
## 5.2. log4j2.yml配置文件

略。
## 5.3. 如何进行验证
1. 通过controller调用，生成日志
```
@RequestMapping("/hello")
public String hello(){
    log.error("hello plat-gateway!");
    return "hello world!" ;
}
```
2. 在kibana控制台检索
```
访问地址：
http://yf009.intdev.hxyd.tech:5601/app/kibana#/home?_g=()
在Discover菜单下检索：
message :'hello plat-gateway'
```
注：请注意检索时间范围。
# 6. 其他