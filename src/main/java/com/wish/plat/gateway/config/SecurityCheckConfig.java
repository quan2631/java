package com.wish.plat.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: QUAN
 * @date: Created in 2019/8/20 13:14
 * @description: 读取配置文件初始化过滤规则
 * @modified By:
 */
@Configuration
@ConfigurationProperties(prefix = "security.check")
@Getter
@Setter
public class SecurityCheckConfig {

    // 是否开启校验
    private boolean check;
    // 是否开启日志
    private boolean log;
    // 是否开启header
    private boolean checkHeader;
    // 是否开启特殊字符
    private boolean checkParameter;
    // 是否开启host
    private boolean checkHost;
    // 是否开启referer，跨站点请求伪造
    private boolean checkReferer;
    // 正则部分
    private List<String> regex;
    // 白名单部分
    private List<String> headerWhiteName;
    private List<String> parameterWhiteName;
    private List<String> hostWhiteName;
    private List<String> refererWhiteName;
    // 命令关键字信息
    private List<String> keyWords;

    // 特殊字符匹配（根据配置的正则生成）
    private static Pattern CHECK_PATTERN;

    /**
     * 匹配则返回true，否则返回false
     * @param input
     * @return
     */
    public boolean matches(String input) {
        Matcher m = CHECK_PATTERN.matcher(input);
        return m.find();
    }
    /**
     * 依赖注入完成后被自动调用
     */
    @PostConstruct
    private void init() {
        StringBuffer regexSb = new StringBuffer();
        for (int i = 0; i < regex.size(); i++) {
            if (i > 0){
                regexSb.append("|");
            }
            regexSb.append(regex.get(i));
        }
        /**
         * 关键字紧跟空格则理解为非法字符
         */
        regexSb.append("|");
        for (int i = 0; i < keyWords.size(); i++) {
            if(i == keyWords.size()-1){
                regexSb.append(keyWords.get(i)).append(" ");
            }else{
                regexSb.append(keyWords.get(i)).append(" |");
            }
        }
        String regex = regexSb.toString();
        CHECK_PATTERN = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
}
