package com.jjbeto.bitmex.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jjbeto.bitmex.client.RFC3339DateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.dataformat.csv.CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS;

@Configuration
public class JacksonConfig {

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
    public CsvMapper csvMapper() {
        final CsvMapper mapper = new CsvMapper();
        mapper.setSerializationInclusion(NON_NULL);
        mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(ALWAYS_QUOTE_STRINGS);
        mapper.enable(WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(READ_ENUMS_USING_TO_STRING);
        mapper.setDateFormat(new RFC3339DateFormat());
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}
