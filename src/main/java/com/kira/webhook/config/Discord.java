package com.kira.webhook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord")
@Getter
@Setter
public class Discord {
    private String secret;
    private String api;
    private String channel;
    private String channel_qa;
    private String channel_prod;
}
