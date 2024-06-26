package com.kira.webhook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
@Getter
@Setter
public class Github {
    private String secret;
    private String api;
}
