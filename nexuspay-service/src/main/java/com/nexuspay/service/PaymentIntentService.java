package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentRequest;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentIntentService {
    
    private final PaymentIntentRepository paymentIntentRepository;
    private final ProviderAccountRepository providerAccountRepository;
    private final RoutingEngine routingEngine;
    private final ProviderDispatcher providerDispatcher;
    
    @Transactional
    public PaymentIntent create(UUID merchantId, CreateRequest req) {
        if (req.idempotencyKey() != null) {
            var existing = paymentIntentRepository.findByMerchantIdAndIdempotencyKey(merchantId, req.idempotencyKey());
            if (existing.isPresent()) return existing.get();
        }
        
        PaymentIntent intent = new PaymentIntent();
        intent.setMerchantId(merchantId);
        intent.setMode(req.mode() != null ? req.mode() : PaymentIntent.Mode.TEST);
        intent.setAmount(req.amount());
        intent.setCurrency(req.currency().toLowerCase());
        intent.setStatus(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD);
        intent.setCaptureMethod(req.captureMethod() != null ? req.captureMethod() : PaymentIntent.CaptureMethod.AUTOMATIC);
        intent.setIdempotencyKey(req.idempotencyKey() != null ? req.idempotencyKey() : UUID.randomUUID().toString());
        intent.setMetadata(req.metadata());
        intent.setOrderId(req.orderId());
        intent.setDescription(req.description());
        intent.setSuccessUrl(req.successUrl());
        intent.setCancelUrl(req.cancelUrl());
        
        return paymentIntentRepository.save(intent);
    }
    
    @Transactional
    public PaymentIntent confirm(UUID merchantId, UUID intentId, ConfirmRequest req) {
        PaymentIntent intent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND));
        
        if (!intent.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND);
        }
        
        if (intent.getStatus() != PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD) {
            throw new BusinessException("Invalid payment status", HttpStatus.BAD_REQUEST);
        }
        
        RoutingEngine.RoutingResult routing = routingEngine.resolve(
                merchantId, intent.getAmount(), intent.getCurrency(),
                null, req.paymentMethodType(), intent.getMode());
        
        if (routing == null || routing.primary() == null) {
            throw new BusinessException("No available provider", HttpStatus.BAD_REQUEST);
        }
        
        ProviderAccount account = routing.primary();
        intent.setResolvedProvider(account.getProvider());
        intent.setConnectorAccountId(account.getId());
        intent.setPaymentMethodType(req.paymentMethodType());
        intent.setStatus(PaymentIntent.PaymentStatus.PROCESSING);
        
        ChargeResult result = providerDispatcher.charge(account.getProvider(), intent, req.paymentMethodId());
        
        intent.setProviderPaymentId(result.providerPaymentId());
        intent.setProviderResponse(result.providerResponse());
        
        if (result.success()) {
            intent.setStatus(intent.getCaptureMethod() == PaymentIntent.CaptureMethod.MANUAL 
                    ? PaymentIntent.PaymentStatus.REQUIRES_CAPTURE 
                    : PaymentIntent.PaymentStatus.SUCCEEDED);
        } else {
            intent.setStatus(PaymentIntent.PaymentStatus.FAILED);
        }
        
        return paymentIntentRepository.save(intent);
    }
    
    @Transactional
    public PaymentIntent capture(UUID merchantId, UUID intentId) {
        PaymentIntent intent = getPaymentIntent(merchantId, intentId);
        
        if (intent.getStatus() != PaymentIntent.PaymentStatus.REQUIRES_CAPTURE) {
            throw new BusinessException("Invalid payment status for capture", HttpStatus.BAD_REQUEST);
        }
        
        boolean success = providerDispatcher.capture(intent.getResolvedProvider(), 
                intent.getProviderPaymentId(), intent.getConnectorAccountId());
        
        if (success) {
            intent.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);
        } else {
            intent.setStatus(PaymentIntent.PaymentStatus.FAILED);
        }
        
        return paymentIntentRepository.save(intent);
    }
    
    @Transactional
    public PaymentIntent cancel(UUID merchantId, UUID intentId) {
        PaymentIntent intent = getPaymentIntent(merchantId, intentId);
        
        if (intent.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED) {
            throw new BusinessException("Cannot cancel succeeded payment", HttpStatus.BAD_REQUEST);
        }
        
        if (intent.getProviderPaymentId() != null) {
            providerDispatcher.cancel(intent.getResolvedProvider(), 
                    intent.getProviderPaymentId(), intent.getConnectorAccountId());
        }
        
        intent.setStatus(PaymentIntent.PaymentStatus.CANCELED);
        return paymentIntentRepository.save(intent);
    }
    
    public PaymentIntent getPaymentIntent(UUID merchantId, UUID intentId) {
        PaymentIntent intent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND));
        
        if (!intent.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND);
        }
        
        return intent;
    }
    
    public List<PaymentIntent> listPaymentIntents(UUID merchantId) {
        return paymentIntentRepository.findByMerchantId(merchantId);
    }
    
    public record CreateRequest(BigInteger amount, String currency, PaymentIntent.Mode mode,
                               PaymentIntent.CaptureMethod captureMethod, String idempotencyKey,
                               String metadata, String orderId, String description,
                               String successUrl, String cancelUrl) {}
    
    public record ConfirmRequest(String paymentMethodType, String paymentMethodId) {}
    
    public record ChargeResult(boolean success, String providerPaymentId, String providerResponse,
                              String failureCode, String failureMessage) {}
}
