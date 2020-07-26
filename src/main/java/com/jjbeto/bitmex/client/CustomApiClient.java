package com.jjbeto.bitmex.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjbeto.bitmex.client.auth.Authentication;
import com.jjbeto.bitmex.config.AppProperties;
import com.jjbeto.bitmex.service.BitmexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

public class CustomApiClient extends ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CustomApiClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final BitmexService bitmexService;

    public CustomApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, AppProperties appProperties, BitmexService bitmexService) {
        super(restTemplate);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.bitmexService = bitmexService;

        setBasePath(this.appProperties.getUrl());
    }

    @Override
    public <T> ResponseEntity<T> invokeAPI(String path, HttpMethod method, MultiValueMap<String, String> queryParams, Object body, HttpHeaders headerParams, MultiValueMap<String, Object> formParams, List<MediaType> accept, MediaType contentType, String[] authNames, ParameterizedTypeReference<T> returnType) throws RestClientException {
        updateParamsForAuth(authNames, queryParams, headerParams);

        final UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBasePath()).path(path);
        if (queryParams != null) {
            builder.queryParams(queryParams);
        }

        final RequestEntity.BodyBuilder requestBuilder = RequestEntity.method(method, builder.build().toUri());
        if (accept != null) {
            requestBuilder.accept(accept.toArray(new MediaType[accept.size()]));
        }
        if (contentType != null) {
            requestBuilder.contentType(contentType);
        }

        addHeadersToRequest(headerParams, requestBuilder);

        final String requestPath = builder.build().toString().replace(getBasePath(), "/api/v1");
        final Object contentBody = selectBody(body, formParams, contentType);
        authorizeCall(method.name(), requestPath, contentBody, requestBuilder);
        final RequestEntity<Object> requestEntity = requestBuilder.body(contentBody);
        ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, returnType);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity;
        } else {
            // The error handler built into the RestTemplate should handle 400 and 500 series errors.
            throw new RestClientException("API returned " + responseEntity.getStatusCode() + " and it wasn't handled by the RestTemplate error handler");
        }
    }

    private void updateParamsForAuth(String[] authNames, MultiValueMap<String, String> queryParams, HttpHeaders headerParams) {
        for (String authName : authNames) {
            Authentication auth = getAuthentications().get(authName);
            if (auth == null) {
                throw new RestClientException("Authentication undefined: " + authName);
            }
            auth.applyToParams(queryParams, headerParams);
        }
    }

    @Override
    protected Object selectBody(Object obj, MultiValueMap<String, Object> formParams, MediaType contentType) {
        final boolean isForm = MULTIPART_FORM_DATA.isCompatibleWith(contentType)
                || APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType);
        if (isForm) {
            return formParams.entrySet().stream()
                    // has value
                    .filter(e -> e.getValue() != null && e.getValue().size() > 0)
                    // concat
                    .map(e -> e.getKey() + "=" + e.getValue().get(0))
                    // join
                    .collect(joining("&"));
        } else {
            return obj;
        }
    }

    /**
     * @param verb Http verb (GET, POST, PUT or DELETE)
     * @param path URL path
     * @param data json call body
     * @return
     */
    public void authorizeCall(String verb, String path, Object data, RequestEntity.BodyBuilder requestBuilder) {
        try {
            final OffsetDateTime utc = OffsetDateTime.now(UTC);
            final String apiExpires = String.valueOf(utc.toEpochSecond() + 15);
            final String body = data == null ?
                    "" :
                    (data instanceof String ? (String) data : objectMapper.writeValueAsString(data));
            final String signature = bitmexService.createSignature(verb + path + apiExpires + body);

            requestBuilder.header("api-key", appProperties.getKey());
            requestBuilder.header("api-expires", apiExpires);
            requestBuilder.header("api-signature", signature);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
