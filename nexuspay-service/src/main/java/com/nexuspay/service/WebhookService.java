package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.WebhookEndpoint;
import com.nexuspay.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookService {
    
    private final WebhookEndpointRepository webhookEndpointRepository;
    private final CryptoUtil cryptoUtil;
    
    @Transactional
    public WebhookEndpoint create(UUID merchantId, CreateWebhookRequest req) {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setMerchantId(merchantId);
        endpoint.setUrl(req.url());
        endpoint.setEvents(req.events());
        endpoint.setSigningSecret(cryptoUtil.generateToken().substring(0, 32));
        endpoint.setStatus(WebhookEndpoint.EndpointStatus.ACTIVE);
        
        return webhookEndpointRepository.save(endpoint);
    }
    
    @Transactional
    public WebhookEndpoint update(UUID endpointId, UpdateWebhookRequest req) {
        WebhookEndpoint endpoint = webhookEndpointRepository.findById(endpointId)
                .orElseThrow(() -> new BusinessException("Webhook endpoint not found", HttpStatus.NOT_FOUND));
        
        if (req.url() != null) endpoint.setUrl(req.url());
        if (req.events() != null) endpoint.setEvents(req.events());
        if (req.status() != null) endpoint.setStatus(req.status());
        
        return webhookEndpointRepository.save(endpoint);
    }
    
    public List<WebhookEndpoint> listEndpoints(UUID merchantId) {
        return webhookEndpointRepository.findByMerchantId(merchantId);
    }
    
    public WebhookEndpoint getEndpoint(UUID endpointId) {
        return webhookEndpointRepository.findById(endpointId)
                .orElseThrow(() -> new BusinessException("Webhook endpoint not found", HttpStatus.NOT_FOUND));
    }
    
    @Transactional
    public void delete(UUID endpointId) {
        webhookEndpointRepository.deleteById(endpointId);
    }
    
    public record CreateWebhookRequest(String url, String events) {}
    public record UpdateWebhookRequest(String url, String events, WebhookEndpoint.EndpointStatus status) {}
}
