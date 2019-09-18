package com.wish.plat.gateway.adapt.sofa;

import com.alipay.hessian.generic.model.GenericObject;
import com.alipay.sofa.rpc.api.GenericService;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.wish.plat.gateway.adapt.ProtocolAdapt;
import com.wish.plat.gateway.routeStore.service.DynamicRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *针对sofa 协议适配的实现类
 */
@Component
public class SofaProtocolAdapt implements ProtocolAdapt {

    private final static Logger LOGGER = LoggerFactory.getLogger(SofaProtocolAdapt.class);

    // sofa rpc jar 的配置类 appName、appId, insId
    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private RegistryConfig registryConfig;

    @Resource
    private DynamicRouteService routeService;


    @Override
    public Object doGenericInvoke(String interfaceClass, String methodName, List<Map<String, Object>> params) {
        /**
         * 根据接口类获取直连地址
         */
        String directUrl = routeService.getDirectUrl(interfaceClass);
        /**
         * sofa rpc jar的通用service对象
         * 通过缓存获取，提高访问效率
         */
        GenericService genericService = SofaServiceCache.getService(interfaceClass, directUrl, applicationConfig, registryConfig);


        List<String> types = new ArrayList<>();
        List<Object> args = new ArrayList<>();

        // 参数转换
        for (Map<String, Object> param : params) {
            for (Map.Entry<String, Object> paramMap : param.entrySet()) {
                // hessian jar里的一个通用对象
                GenericObject genericObject = new GenericObject(paramMap.getKey());
                if (paramMap.getValue() instanceof Map){
                    Map<String, Object> attrubits = (Map)paramMap.getValue();
                    for (Map.Entry<String, Object> attrubit : attrubits.entrySet()) {
                        genericObject.putField(attrubit.getKey(), attrubit.getValue());
                    }
                    types.add(paramMap.getKey());
                    args.add(genericObject);
                }else {
                    types.add(paramMap.getKey());
                    args.add(paramMap.getValue());
                }
            }
        }

        // 泛化执行
        Object genericObjectInvoke = null;
        String[] genericTypes =new String[types.size()];

        try {
            genericObjectInvoke = genericService.$genericInvoke(methodName,
                    types.toArray(genericTypes),
                    args.toArray());
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        return genericObjectInvoke;
    }
}
