package com.nexuspay.infra.webhook;

import com.nexuspay.service.provider.WebhookTransportPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class HttpWebhookTransportAdapter implements WebhookTransportPort {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void postJson(String url, Object payload, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }

        HttpEntity<Object> request = new HttpEntity<>(payload, httpHeaders);
        restTemplate.postForEntity(url, request, String.class);
    }
}