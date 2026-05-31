package com.auditoria.orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiServiceWebClient(
            @Value("${app.ai-service.url}") String aiServiceUrl,
            @Value("${app.ai-service.api-key}") String apiKey) {
        return WebClient.builder()
                .baseUrl(aiServiceUrl)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }
}
