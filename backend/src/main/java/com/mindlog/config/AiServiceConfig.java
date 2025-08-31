package com.mindlog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ai.service")
public class AiServiceConfig {

    // Getters and Setters
    private String url = "http://localhost:11434";
    private String model = "gemma3:4b";
    private int timeout = 30000;
    private String tone = "direct";

}
