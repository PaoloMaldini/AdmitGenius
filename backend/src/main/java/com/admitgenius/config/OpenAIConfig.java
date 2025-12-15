package com.admitgenius.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {

    // 直接从配置文件读取，不再使用环境变量
    @Value("${openai.api.key:}")
    private String apiKeyFromConfig;

    private ApiConfig api = new ApiConfig();
    private String proxyUrl;

    @PostConstruct
    public void init() {
        // 优先使用配置文件中的值
        if (apiKeyFromConfig != null && !apiKeyFromConfig.trim().isEmpty()
                && !apiKeyFromConfig.contains("${")) {
            api.setKey(apiKeyFromConfig);
        }

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