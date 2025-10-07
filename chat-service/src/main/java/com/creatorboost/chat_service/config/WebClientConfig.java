package com.creatorboost.chat_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // You can set a base URL for auth-service if you want
        return builder
                .baseUrl("http://localhost:8080")
                .build();
    }
}
