package com.wish.plat.gateway.adapt;

import java.util.List;
import java.util.Map;

/**
 * 协议适配接口
 */
public interface ProtocolAdapt {

    /**
     * 做通用的调用方法
     * 包括类名、调用的方法、携带的参数
     *
     * @param interfaceClass
     * @param methodName
     * @param params
     * @return
     */
    public Object doGenericInvoke(String interfaceClass, String methodName, List<Map<String, Object>> params);

}
