package com.cshp.aiservice2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.qianwen")
public class QianwenConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
    private Integer maxTokens;
    private Double temperature;
}