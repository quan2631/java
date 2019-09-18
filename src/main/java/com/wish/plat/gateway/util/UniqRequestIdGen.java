package com.wish.plat.gateway.util;


import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * Bao de yu
 */
public class UniqRequestIdGen {
    public  static Logger Log=LogManager.getLogger(UniqRequestIdGen.class);
    // 自增id，用于requestId的生成过程
    private static AtomicLong lastId  = new AtomicLong();
    private static final Long MAX = 999999L;
    // 本机ip地址，用于requestId的生成过程
    private static final String ip  = LocalIpAddressUtil.resolveLocalAddress();
    private static final String path  = System.getProperty("user.dir");

    public synchronized static String resolveReqId(int cd) {
        if(lastId.get()>=MAX){
            lastId.set(0);
        }
        return hexIp(ip) + handleNum(lastId.incrementAndGet(),hexIp(ip),cd);
    }

    // 取ip最后一段
    private static String hexIp(String ip) {
        StringBuilder sb = new StringBuilder();
        String[] ips = ip.split("\\.");
        sb.append(ips[ips.length-1]);
        return sb.toString();
    }
    private static String handleNum(Long number,String ip,int cd){
        String numberStr = String.valueOf(number);
        StringBuilder numberSB = new StringBuilder();
        for(int i=0;i<((cd-ip.length())-numberStr.length());i++){
            numberSB.append("0");
        }
        return numberSB.append(numberStr).toString();
    }

    private static class LocalIpAddressUtil {

        /**
         * 获取本地ip地址，有可能会有多个地址, 若有多个网卡则会搜集多个网卡的ip地址
         */
        public static  Set<InetAddress> resolveLocalAddresses() {
            Set<InetAddress> addrs = new HashSet<InetAddress>();
            Enumeration<NetworkInterface> ns = null;
            try {
                ns = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e) {
                // ignored...
            }
            while (ns != null && ns.hasMoreElements()) {
                NetworkInterface n = ns.nextElement();
                Enumeration<InetAddress> is = n.getInetAddresses();
                while (is.hasMoreElements()) {
                    InetAddress i = is.nextElement();
                    if (!i.isLoopbackAddress() && !i.isLinkLocalAddress() && !i.isMulticastAddress()
                            && !isSpecialIp(i.getHostAddress())) addrs.add(i);
                }
            }
            return addrs;
        }

        public static  Set<String> resolveLocalIps() {
            Set<InetAddress> addrs = resolveLocalAddresses();
            Set<String> ret = new HashSet<String>();
            for (InetAddress addr : addrs)
                ret.add(addr.getHostAddress());
            return ret;
        }

        private static boolean isSpecialIp(String ip) {
            if (ip.contains(":")) return true;
            if (ip.startsWith("127.")) return true;
            if (ip.startsWith("169.254.")) return true;
            if (ip.equals("255.255.255.255")) return true;
            return false;
        }

        public static String resolveLocalAddress(){
            Set<String> ipSet = resolveLocalIps();
            String ip = "127.0.0.1";
            if (ipSet.size() != 0) {
                for (String ips : ipSet) {
                    ip = ips;
                    break;
                }
            }
            return ip;
        }

    }
    public static String getkey(){
        String key=null;
        String[] paths=path.split("\\"+File.separator);
        key=paths[paths.length-1]+"_"+DateUtil.getDateNow("yyyyMMddHHmmss")+UniqRequestIdGen.resolveReqId(8);
        return key;
    }
    public static String getkeyid(){
        String key=null;
        key=DateUtil.getDateNow("yyMMddHHmmss")+UniqRequestIdGen.resolveReqId(2);
        return key;
    }
    public static String geServername(){
        String key=null;
        String[] paths=path.split("\\"+File.separator);
        key=paths[paths.length-1];
        return key;
    }
    public static String gettracerId(){
        String id="";
        SofaTraceContext sofaTraceContext=null;
        SofaTracerSpan sofaTracerSpan=null;
        SofaTracerSpanContext sofaTracerSpanContext=null;
        try{
            sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            sofaTracerSpan = sofaTraceContext.getCurrentSpan();
            sofaTracerSpanContext = sofaTracerSpan.getSofaTracerSpanContext();
            id=sofaTracerSpanContext.getTraceId();

            //	String traceId = sofaTracerSpanContext.getTraceId();
        }catch (Exception e) {
            id=getkeyid();
            Log.error(e);
        }
        return id;
    }
}

