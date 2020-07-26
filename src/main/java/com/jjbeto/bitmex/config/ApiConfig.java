package com.jjbeto.bitmex.config;

import com.jjbeto.bitmex.client.ApiClient;
import com.jjbeto.bitmex.client.CustomApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("!test")
public class ApiConfig {

    private final AppProperties appProperties;

    public ApiConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Primary
    @Bean("com.jjbeto.bitmex.client.ApiClient")
    public ApiClient apiClient(RestTemplate restTemplate) {
        return new CustomApiClient(restTemplate, appProperties);
    }

}
