package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.aggregate.payment.PaymentIntentAggregate;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.domain.service.PaymentDomainService;
import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.domain.valueobject.PaymentStatus;
import com.nexuspay.domain.valueobject.ProviderType;
import com.nexuspay.repository.PaymentIntentRepository;
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
    private final PaymentDomainService paymentDomainService;
    private final RoutingEngine routingEngine;
    private final ProviderDispatcher providerDispatcher;
    private final OutboxService outboxService;

    @Transactional
    public PaymentIntent create(UUID merchantId, CreateRequest req) {
        if (req.idempotencyKey() != null) {
            var existing = paymentIntentRepository.findByMerchantIdAndIdempotencyKey(merchantId, req.idempotencyKey());
            if (existing.isPresent()) return existing.get();
        }

        PaymentIntentAggregate aggregate = paymentDomainService.createPaymentIntent(
                merchantId,
                Money.of(req.amount(), req.currency()),
                req.idempotencyKey() != null ? req.idempotencyKey() : UUID.randomUUID().toString(),
                req.captureMethod() == PaymentIntent.CaptureMethod.MANUAL);

        PaymentIntent intent = toEntity(aggregate);
        intent.setMode(req.mode() != null ? req.mode() : PaymentIntent.Mode.TEST);
        intent.setMetadata(req.metadata());
        intent.setOrderId(req.orderId());
        intent.setDescription(req.description());
        intent.setSuccessUrl(req.successUrl());
        intent.setCancelUrl(req.cancelUrl());

        PaymentIntent saved = paymentIntentRepository.save(intent);
        publishDomainEvents(aggregate, saved);
        return saved;
    }

    @Transactional
    public PaymentIntent confirm(UUID merchantId, UUID intentId, ConfirmRequest req) {
        PaymentIntent intent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND));

        if (!intent.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND);
        }

        PaymentIntentAggregate aggregate = toAggregate(intent);

        RoutingEngine.RoutingResult routing = routingEngine.resolve(
                merchantId, intent.getAmount(), intent.getCurrency(),
                null, req.paymentMethodType(), intent.getMode());

        if (routing == null || routing.primary() == null) {
            throw new BusinessException("No available provider", HttpStatus.BAD_REQUEST);
        }

        ProviderAccount account = routing.primary();
        ProviderType provider = ProviderType.valueOf(account.getProvider().name());

        // State validation + transition via domain service
        paymentDomainService.confirmPayment(aggregate, req.paymentMethodType(), provider, account.getId());
        syncToEntity(aggregate, intent);

        ChargeResult result = providerDispatcher.charge(account.getProvider(), intent, req.paymentMethodId());

        intent.setProviderPaymentId(result.providerPaymentId());
        intent.setProviderResponse(result.providerResponse());

        if (result.success()) {
            paymentDomainService.markPaymentSucceeded(aggregate, result.providerPaymentId());
        } else {
            paymentDomainService.markPaymentFailed(aggregate,
                    result.failureCode() != null ? result.failureCode() : "unknown",
                    result.failureMessage() != null ? result.failureMessage() : "Payment failed",
                    false);
        }
        syncToEntity(aggregate, intent);

        PaymentIntent saved = paymentIntentRepository.save(intent);
        publishDomainEvents(aggregate, saved);
        return saved;
    }

    @Transactional
    public PaymentIntent capture(UUID merchantId, UUID intentId) {
        PaymentIntent intent = getPaymentIntent(merchantId, intentId);
        PaymentIntentAggregate aggregate = toAggregate(intent);

        // State validation + transition via aggregate
        aggregate.capture();
        syncToEntity(aggregate, intent);

        boolean success = providerDispatcher.capture(toAccountProvider(intent.getResolvedProvider()),
                intent.getProviderPaymentId(), intent.getConnectorAccountId());

        if (!success) {
            intent.setStatus(PaymentIntent.PaymentStatus.FAILED);
        }

        PaymentIntent saved = paymentIntentRepository.save(intent);
        publishDomainEvents(aggregate, saved);
        return saved;
    }

    @Transactional
    public PaymentIntent cancel(UUID merchantId, UUID intentId) {
        PaymentIntent intent = getPaymentIntent(merchantId, intentId);
        PaymentIntentAggregate aggregate = toAggregate(intent);

        // State validation + transition via aggregate
        aggregate.cancel();
        syncToEntity(aggregate, intent);

        if (intent.getProviderPaymentId() != null) {
            providerDispatcher.cancel(toAccountProvider(intent.getResolvedProvider()),
                    intent.getProviderPaymentId(), intent.getConnectorAccountId());
        }

        PaymentIntent saved = paymentIntentRepository.save(intent);
        publishDomainEvents(aggregate, saved);
        return saved;
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

    // ---- records ----

    public record CreateRequest(BigInteger amount, String currency, PaymentIntent.Mode mode,
                               PaymentIntent.CaptureMethod captureMethod, String idempotencyKey,
                               String metadata, String orderId, String description,
                               String successUrl, String cancelUrl) {}

    public record ConfirmRequest(String paymentMethodType, String paymentMethodId) {}

    public record ChargeResult(boolean success, String providerPaymentId, String providerResponse,
                              String failureCode, String failureMessage) {}

    // ---- aggregate <-> entity mapping ----

    private PaymentIntentAggregate toAggregate(PaymentIntent entity) {
        return PaymentIntentAggregate.reconstruct(
                entity.getId(), entity.getMerchantId(),
                Money.of(entity.getAmount(), entity.getCurrency()),
                entity.getIdempotencyKey(),
                entity.getCaptureMethod() == PaymentIntent.CaptureMethod.MANUAL,
                PaymentStatus.valueOf(entity.getStatus().name()),
                entity.getResolvedProvider() != null ? ProviderType.valueOf(entity.getResolvedProvider().name()) : null,
                entity.getConnectorAccountId(),
                entity.getProviderPaymentId(),
                entity.getPaymentMethodType());
    }

    private void syncToEntity(PaymentIntentAggregate aggregate, PaymentIntent entity) {
        entity.setStatus(PaymentIntent.PaymentStatus.valueOf(aggregate.getStatus().name()));
        if (aggregate.getResolvedProvider() != null) {
            entity.setResolvedProvider(PaymentIntent.Provider.valueOf(aggregate.getResolvedProvider().name()));
        }
        entity.setConnectorAccountId(aggregate.getConnectorAccountId());
        entity.setProviderPaymentId(aggregate.getProviderPaymentId());
        entity.setPaymentMethodType(aggregate.getPaymentMethodType());
    }

    private PaymentIntent toEntity(PaymentIntentAggregate aggregate) {
        PaymentIntent entity = new PaymentIntent();
        entity.setId(aggregate.getId());
        entity.setMerchantId(aggregate.getMerchantId());
        entity.setAmount(aggregate.getAmount().amount());
        entity.setCurrency(aggregate.getAmount().currency().getCurrencyCode());
        entity.setStatus(PaymentIntent.PaymentStatus.valueOf(aggregate.getStatus().name()));
        entity.setIdempotencyKey(aggregate.getIdempotencyKey());
        entity.setCaptureMethod(aggregate.isManualCapture()
                ? PaymentIntent.CaptureMethod.MANUAL
                : PaymentIntent.CaptureMethod.AUTOMATIC);
        return entity;
    }

    private void publishDomainEvents(PaymentIntentAggregate aggregate, PaymentIntent saved) {
        aggregate.pullDomainEvents().forEach(event -> {
            if (event instanceof com.nexuspay.domain.event.PaymentSucceededEvent) {
                outboxService.createPaymentEvent(saved, "payment_intent.succeeded");
            } else if (event instanceof com.nexuspay.domain.event.PaymentFailedEvent) {
                outboxService.createPaymentEvent(saved, "payment_intent.failed");
            }
        });
    }

    private PaymentIntent.Provider toPaymentProvider(ProviderAccount.Provider provider) {
        return PaymentIntent.Provider.valueOf(provider.name());
    }

    private ProviderAccount.Provider toAccountProvider(PaymentIntent.Provider provider) {
        return provider != null ? ProviderAccount.Provider.valueOf(provider.name()) : null;
    }
}