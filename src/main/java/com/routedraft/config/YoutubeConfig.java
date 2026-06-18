package com.routedraft.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class YoutubeConfig {

    @Value("${youtube.api.base-url}")
    private String baseUrl;

    @Value("${youtube.api-key}")
    private String apiKey;

    /**
     * 유튜브 API 전용 WebClient 빈 등록
     */
    @Bean
    public WebClient youtubeWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }
}