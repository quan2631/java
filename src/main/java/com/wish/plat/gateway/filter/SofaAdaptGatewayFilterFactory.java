package com.wish.plat.gateway.filter;


import com.alibaba.fastjson.TypeReference;
import com.alipay.hessian.generic.model.GenericObject;
import com.alipay.sofa.rpc.common.json.JSON;
import com.wish.plat.gateway.adapt.sofa.SofaProtocolAdapt;
import com.wish.plat.gateway.vo.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class SofaAdaptGatewayFilterFactory extends AbstractGatewayFilterFactory<SofaAdaptGatewayFilterFactory.Config>{

    @Autowired
    private SofaProtocolAdapt sofaProtocolAdapt;

    public SofaAdaptGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(
                "enabled",
                "method",
                "inputParams",
                "protocolType",
                "descr",
                "example",
                "interfaceName"
        );
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }

            // 获取请求
            ServerHttpRequest request = exchange.getRequest();

            // 判断该请求是否为post，只支持post
            if (request.getMethodValue().equals("GET")) {
                ServerHttpResponse response = exchange.getResponse();
                ResponseResult respResult = new ResponseResult();
                respResult.setCode(HttpStatus.BAD_REQUEST.value());
                respResult.setMessage("Get Method not adapt, please use Post");
                byte[] datas = JSON.toJSONString(respResult).getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = response.bufferFactory().wrap(datas);
                response.setStatusCode(HttpStatus.BAD_REQUEST);
                response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                return response.writeWith(Mono.just(buffer));
            }

            final String interfaceName = config.getInterfaceName();
            final String method = config.getMethod();

            // 获取请求参数并转换

            ServerRequest serverRequest = new DefaultServerRequest(exchange);
            return serverRequest.bodyToMono(String.class).flatMap(body -> {
                ServerHttpResponse response = exchange.getResponse();

                Map<String, Object> requestObject = com.alibaba.fastjson.JSON.parseObject(body, new TypeReference<Map<String, Object>>() {
                });
                List<Map<String, Object>> args = (List<Map<String, Object>>)requestObject.get("params");

                /**
                 * 进行sofa调用
                 */
                Object genericInvoke = null;
                // config中有入参定义，前提是，定义这个参数的时候，需要知道参数对应的参数类型
                String inputParams = config.getInputParams();
                if (inputParams != null) {
                    Map<String, String> convertParams = JSON.parseObject(inputParams, Map.class);

                    List<Map<String, Object>> newParams = new ArrayList<>();
                    args.forEach(arg -> {
                        arg.entrySet().forEach(type ->{
                            convertParams.entrySet().forEach(inputParam -> {
                                if (inputParam.getKey().equals(type.getKey())) {
                                    Map<String, Object> newP = new HashMap<>();
                                    newP.put(inputParam.getValue(), type.getValue());                    newParams.add(newP);
                                }
                            });
                        });
                    });
                    genericInvoke = sofaProtocolAdapt.doGenericInvoke(interfaceName, method, newParams);
                }else{
                    genericInvoke = sofaProtocolAdapt.doGenericInvoke(interfaceName, method, args);
                }


                GenericObject genericObject = null;
                try {
                    genericObject = (GenericObject) genericInvoke;
                } catch (Exception e) {
                    /**
                     * 如果转换异常，说明返回时基础类型。直接返回
                     */
                    String jsonString = JSON.toJSONString(genericInvoke);
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return response.writeWith(Flux.just(response.bufferFactory().wrap(jsonString.getBytes())));
                }
                if(null == genericObject){
                    log.error("GenericObject is null!");
                    throw new RuntimeException("GenericObject is null!");
                }
                /**
                 * 格式化sofa返回的数据
                 * QX 20190814
                 */
                Map<String, Object> resultMap = clearSOFAData(genericObject.getFields());
                // 在返回后转JSON
                String result = JSONObject.fromObject(resultMap).toString();

                response.setStatusCode(HttpStatus.OK);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return response.writeWith(Flux.just(response.bufferFactory().wrap(result.getBytes())));
            });
        };
    }
    /**
     * 将sofa泛化的返回值调整为正常的JSON格式
     * 总入口
     * @param sofaMap
     * @return
     */
    private Map<String, Object> clearSOFAData(Map<String, Object> sofaMap){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (String key: sofaMap.keySet()){
            Object v = sofaMap.get(key);
            if (v instanceof GenericObject){
                // 如果是sofa对象
                GenericObject sofaObj = (GenericObject) v;
                resultMap.put(key, clearSOFAData(sofaObj.getFields()));
            }else if (v instanceof Map) {
                // 如果是内嵌对象
                Map<String, Object> otherMap = (Map) v;
                resultMap.put(key, clearSOFAData(otherMap));
            }else if (v instanceof ArrayList) {
                // 如果数组对象
                List<Map<String, Object>> list = array2List((ArrayList)v);
                resultMap.put(key, list);
            }else {
                // 如果是基础类型
                resultMap.put(key, v);
            }
        }
        return  resultMap;
    }
    /**
     * 建一个数组对象转为List
     * @param list
     * @return
     */
    private List array2List(ArrayList list){
        List retList = new ArrayList();
        if(list!=null||list.size()!=0) {
            for (int i = 0; i < list.size(); i++) {
                Object one = clearSOFADataInList(list.get(i));
                retList.add(one);
            }
        }
        return retList;
    }
    /**
     * 处理集合里边的对象
     * @param v
     * @return
     */
    private Object clearSOFADataInList(Object v){
        if (v instanceof GenericObject){
            // 如果是sofa对象
            GenericObject sofaObj = (GenericObject) v;
            // 如果集合里的对象继续嵌套sofa对象
            return clearSOFAData(sofaObj.getFields());
        }else {
            // 如果是基础类型
            return v.toString();
        }
    }

    public static class Config {
        /**
         * 是否启用
         */
        private boolean enabled;
        /**
         * 协议类型
         */
        private String protocolType;
        /**
         * 描述
         */
        private String descr;
        /**
         * 调用示例
         */
        private String example;
        /**
         * 接口名称
         */
        private String interfaceName;
        /**
         * 方法名称
         */
        private String method;
        /**
         * 输入参数
         */
        private String inputParams;

        public String getInterfaceName() {
            return interfaceName;
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public boolean isEnabled() {
            return enabled;
        }
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getInputParams() {
            return inputParams;
        }

        public void setInputParams(String inputParams) {
            this.inputParams = inputParams;
        }

        public String getProtocolType() {
            return protocolType;
        }

        public void setProtocolType(String protocolType) {
            this.protocolType = protocolType;
        }

        public String getDescr() {
            return descr;
        }

        public void setDescr(String descr) {
            this.descr = descr;
        }

        public String getExample() {
            return example;
        }

        public void setExample(String example) {
            this.example = example;
        }
        public Config() {}
    }
}
