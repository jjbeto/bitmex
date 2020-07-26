package com.jjbeto.bitmex.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jjbeto.bitmex.client.ApiClient;
import com.jjbeto.bitmex.client.CustomApiClient;
import com.jjbeto.bitmex.client.RFC3339DateFormat;
import com.jjbeto.bitmex.service.BitmexService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;

@Configuration
@Profile("!test")
public class ApiConfig {

    private final AppProperties appProperties;

    public ApiConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(NON_NULL);
        mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(READ_ENUMS_USING_TO_STRING);
        mapper.setDateFormat(new RFC3339DateFormat());
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        return new RestTemplateBuilder()
                .additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Primary
    @Bean("com.jjbeto.bitmex.client.ApiClient")
    public ApiClient apiClient(RestTemplate restTemplate, ObjectMapper objectMapper, BitmexService bitmexService) {
        return new CustomApiClient(restTemplate, objectMapper, appProperties, bitmexService);
    }

}
