package com.admitgenius.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Setter
@Configuration
@PropertySource("classpath:openai.properties") // 指定自定义配置文件
@ConfigurationProperties(prefix = "openai") // 属性前缀
public class OpenAIConfig {
    private ApiConfig api = new ApiConfig();
    private String proxyUrl; // 对应 openai.proxy.url

    @PostConstruct
    public void init() {
        System.out.println("========== OpenAIConfig 加载完成 ==========");
        System.out.println("openai.api.key: "
                + (api.getKey() != null ? api.getKey().substring(0, Math.min(15, api.getKey().length())) + "..."
                        : "NULL"));
        System.out.println("openai.api.proxyUrl: " + api.getProxyUrl());
        System.out.println("openai.proxy.url: " + proxyUrl);
        System.out.println("============================================");
    }

    @Getter
    @Setter
    public static class ApiConfig {
        private String key;
        private String proxyUrl;

    }
}