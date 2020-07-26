package com.jjbeto.bitmex.client;

import com.jjbeto.bitmex.client.auth.Authentication;
import com.jjbeto.bitmex.config.AppProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class CustomApiClient extends ApiClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    public CustomApiClient(RestTemplate restTemplate, AppProperties appProperties) {
        super(restTemplate);
        this.restTemplate = restTemplate;
        this.appProperties = appProperties;
        setBasePath(this.appProperties.getUrl());
    }

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

        RequestEntity<Object> requestEntity = requestBuilder.body(selectBody(body, formParams, contentType));

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

}
