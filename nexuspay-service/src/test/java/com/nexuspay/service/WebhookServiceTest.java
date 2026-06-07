package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.WebhookEndpoint;
import com.nexuspay.repository.WebhookEndpointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebhookServiceTest {

    @Mock
    private WebhookEndpointRepository webhookEndpointRepository;

    @Mock
    private CryptoUtil cryptoUtil;

    @InjectMocks
    private WebhookService webhookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateWebhookEndpointWithActiveStatusAndSigningSecret() {
        UUID merchantId = UUID.randomUUID();
        var req = new WebhookService.CreateWebhookRequest("https://example.com/webhook", "payment.succeeded");

        when(cryptoUtil.generateToken()).thenReturn("abcdefghijklmnopqrstuvwxyz0123456789TOKEN");
        when(webhookEndpointRepository.save(any(WebhookEndpoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEndpoint result = webhookService.create(merchantId, req);

        assertNotNull(result);
        assertEquals(merchantId, result.getMerchantId());
        assertEquals("https://example.com/webhook", result.getUrl());
        assertEquals("payment.succeeded", result.getEvents());
        assertEquals(WebhookEndpoint.EndpointStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getSigningSecret());
        assertEquals(32, result.getSigningSecret().length());

        verify(cryptoUtil, times(1)).generateToken();
        verify(webhookEndpointRepository, times(1)).save(any(WebhookEndpoint.class));
    }

    @Test
    void shouldUpdateAllProvidedFields() {
        UUID endpointId = UUID.randomUUID();
        WebhookEndpoint existing = new WebhookEndpoint();
        existing.setId(endpointId);
        existing.setUrl("https://old.example.com/webhook");
        existing.setEvents("payment.failed");
        existing.setStatus(WebhookEndpoint.EndpointStatus.ACTIVE);

        var req = new WebhookService.UpdateWebhookRequest(
                "https://new.example.com/webhook",
                "payment.succeeded,payment.failed",
                WebhookEndpoint.EndpointStatus.INACTIVE
        );

        when(webhookEndpointRepository.findById(endpointId)).thenReturn(Optional.of(existing));
        when(webhookEndpointRepository.save(any(WebhookEndpoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEndpoint result = webhookService.update(endpointId, req);

        assertEquals("https://new.example.com/webhook", result.getUrl());
        assertEquals("payment.succeeded,payment.failed", result.getEvents());
        assertEquals(WebhookEndpoint.EndpointStatus.INACTIVE, result.getStatus());

        verify(webhookEndpointRepository).findById(endpointId);
        verify(webhookEndpointRepository).save(existing);
    }

    @Test
    void shouldKeepOriginalValuesWhenUpdateFieldsAreNull() {
        UUID endpointId = UUID.randomUUID();
        WebhookEndpoint existing = new WebhookEndpoint();
        existing.setId(endpointId);
        existing.setUrl("https://old.example.com/webhook");
        existing.setEvents("payment.failed");
        existing.setStatus(WebhookEndpoint.EndpointStatus.ACTIVE);

        var req = new WebhookService.UpdateWebhookRequest(null, null, null);

        when(webhookEndpointRepository.findById(endpointId)).thenReturn(Optional.of(existing));
        when(webhookEndpointRepository.save(any(WebhookEndpoint.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebhookEndpoint result = webhookService.update(endpointId, req);

        assertEquals("https://old.example.com/webhook", result.getUrl());
        assertEquals("payment.failed", result.getEvents());
        assertEquals(WebhookEndpoint.EndpointStatus.ACTIVE, result.getStatus());

        verify(webhookEndpointRepository).save(existing);
    }

    @Test
    void shouldThrowNotFoundWhenUpdateMissingEndpoint() {
        UUID endpointId = UUID.randomUUID();
        var req = new WebhookService.UpdateWebhookRequest("https://new.example.com/webhook", "event", null);

        when(webhookEndpointRepository.findById(endpointId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> webhookService.update(endpointId, req));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Webhook endpoint not found", ex.getMessage());

        verify(webhookEndpointRepository, never()).save(any());
    }

    @Test
    void shouldListEndpointsByMerchantId() {
        UUID merchantId = UUID.randomUUID();
        WebhookEndpoint e1 = new WebhookEndpoint();
        WebhookEndpoint e2 = new WebhookEndpoint();

        when(webhookEndpointRepository.findByMerchantId(merchantId)).thenReturn(List.of(e1, e2));

        List<WebhookEndpoint> result = webhookService.listEndpoints(merchantId);

        assertEquals(2, result.size());
        verify(webhookEndpointRepository).findByMerchantId(merchantId);
    }

    @Test
    void shouldGetEndpointById() {
        UUID endpointId = UUID.randomUUID();
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setId(endpointId);

        when(webhookEndpointRepository.findById(endpointId)).thenReturn(Optional.of(endpoint));

        WebhookEndpoint result = webhookService.getEndpoint(endpointId);

        assertEquals(endpointId, result.getId());
        verify(webhookEndpointRepository).findById(endpointId);
    }

    @Test
    void shouldThrowNotFoundWhenGetEndpointMissing() {
        UUID endpointId = UUID.randomUUID();
        when(webhookEndpointRepository.findById(endpointId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> webhookService.getEndpoint(endpointId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Webhook endpoint not found", ex.getMessage());
    }

    @Test
    void shouldDeleteEndpointById() {
        UUID endpointId = UUID.randomUUID();

        webhookService.delete(endpointId);

        verify(webhookEndpointRepository).deleteById(endpointId);
    }
}
