package com.kira.webhook;

import com.kira.webhook.utils.SendRequestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public SendRequestUtils sendRequestUtils() {
        return new SendRequestUtils();
    }
}
