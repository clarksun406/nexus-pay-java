package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Customer;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentMethod;
import com.nexuspay.domain.entity.Subscription;
import com.nexuspay.domain.service.SubscriptionDomainService;
import com.nexuspay.repository.CustomerRepository;
import com.nexuspay.repository.PaymentMethodRepository;
import com.nexuspay.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentIntentService paymentIntentService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final InvoiceService invoiceService;
    
    @Transactional
    public Subscription create(UUID merchantId, CreateRequest req) {
        Customer customer = customerRepository.findByMerchantIdAndId(merchantId, req.customerId())
                .orElseThrow(() -> new BusinessException("Customer not found", HttpStatus.NOT_FOUND));
        
        PaymentMethod paymentMethod = null;
        if (req.paymentMethodId() != null) {
            paymentMethod = paymentMethodRepository.findByCustomerIdAndId(req.customerId(), req.paymentMethodId())
                    .orElseThrow(() -> new BusinessException("Payment method not found", HttpStatus.NOT_FOUND));
        }
        
        Subscription subscription = new Subscription();
        subscription.setMerchantId(merchantId);
        subscription.setCustomerId(req.customerId());
        subscription.setPaymentMethodId(req.paymentMethodId());
        subscription.setPlanId(req.planId());
        subscription.setName(req.name());
        subscription.setInterval(req.interval());
        subscription.setIntervalCount(req.intervalCount() != null ? req.intervalCount() : 1);
        subscription.setAmount(req.amount());
        subscription.setCurrency(req.currency().toLowerCase());
        subscription.setStatus(Subscription.SubscriptionStatus.INCOMPLETE);
        
        // Set trial period if provided
        if (req.trialDays() != null && req.trialDays() > 0) {
            Instant now = Instant.now();
            subscription.setTrialStart(now);
            subscription.setTrialEnd(now.plus(req.trialDays(), ChronoUnit.DAYS));
            subscription.setStatus(Subscription.SubscriptionStatus.TRIALING);
        }
        
        // Calculate next billing period
        calculateNextPeriod(subscription);
        
        return subscriptionRepository.save(subscription);
    }
    
    @Transactional
    public Subscription activate(UUID merchantId, UUID subscriptionId) {
        Subscription subscription = getSubscription(merchantId, subscriptionId);
        
        if (subscription.getPaymentMethodId() == null) {
            throw new BusinessException("Payment method required", HttpStatus.BAD_REQUEST);
        }
        
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        calculateNextPeriod(subscription);
        
        return subscriptionRepository.save(subscription);
    }
    
    @Transactional
    public Subscription cancel(UUID merchantId, UUID subscriptionId, boolean immediately) {
        Subscription subscription = getSubscription(merchantId, subscriptionId);
        
        if (immediately) {
            subscription.setStatus(Subscription.SubscriptionStatus.CANCELED);
            subscription.setCanceledAt(Instant.now());
        } else {
            subscription.setCancelAtPeriodEnd(true);
        }
        
        return subscriptionRepository.save(subscription);
    }
    
    public Subscription getSubscription(UUID merchantId, UUID subscriptionId) {
        return subscriptionRepository.findByMerchantIdAndId(merchantId, subscriptionId)
                .orElseThrow(() -> new BusinessException("Subscription not found", HttpStatus.NOT_FOUND));
    }
    
    public List<Subscription> listSubscriptions(UUID merchantId) {
        return subscriptionRepository.findByMerchantId(merchantId);
    }
    
    public List<Subscription> listCustomerSubscriptions(UUID customerId) {
        return subscriptionRepository.findByCustomerId(customerId);
    }
    
    public List<Subscription> getDueForRenewal() {
        return subscriptionRepository.findDueForRenewal(Instant.now());
    }

    @Transactional
    public void processDueRenewals() {
        for (Subscription subscription : getDueForRenewal()) {
            try {
                renew(subscription);
            } catch (Exception e) {
                subscription.setStatus(Subscription.SubscriptionStatus.PAST_DUE);
                subscriptionRepository.save(subscription);
            }
        }
    }

    private void renew(Subscription subscription) {
        if (Boolean.TRUE.equals(subscription.getCancelAtPeriodEnd())) {
            subscription.setStatus(Subscription.SubscriptionStatus.CANCELED);
            subscription.setCanceledAt(Instant.now());
            subscriptionRepository.save(subscription);
            return;
        }

        if (subscription.getPaymentMethodId() == null) {
            subscription.setStatus(Subscription.SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(subscription);
            return;
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findByCustomerIdAndId(
                        subscription.getCustomerId(), subscription.getPaymentMethodId())
                .orElseThrow(() -> new BusinessException("Payment method not found", HttpStatus.NOT_FOUND));

        PaymentIntent intent = paymentIntentService.create(subscription.getMerchantId(),
                new PaymentIntentService.CreateRequest(
                        java.math.BigInteger.valueOf(subscription.getAmount()),
                        subscription.getCurrency(),
                        PaymentIntent.Mode.TEST,
                        PaymentIntent.CaptureMethod.AUTOMATIC,
                        "sub_" + subscription.getId() + "_" + subscription.getCurrentPeriodEnd(),
                        null,
                        subscription.getPlanId(),
                        subscription.getName(),
                        null,
                        null
                ));

        PaymentIntent confirmed = paymentIntentService.confirm(subscription.getMerchantId(), intent.getId(),
                new PaymentIntentService.ConfirmRequest(paymentMethod.getType().name().toLowerCase(), paymentMethod.getProviderPaymentMethodId()));

        if (confirmed.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED) {
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            calculateNextPeriod(subscription);
            // Auto-generate invoice for successful renewal
            invoiceService.createFromSubscription(subscription, confirmed, "subscription_cycle");
        } else {
            subscription.setStatus(Subscription.SubscriptionStatus.PAST_DUE);
        }
        subscriptionRepository.save(subscription);
    }
    
    private void calculateNextPeriod(Subscription subscription) {
        var period = subscriptionDomainService.calculateNextPeriod(
                Instant.now(), subscription.getInterval().name(), subscription.getIntervalCount());
        subscription.setCurrentPeriodStart(period.start());
        subscription.setCurrentPeriodEnd(period.end());
    }
    
    public record CreateRequest(
            UUID customerId,
            UUID paymentMethodId,
            String planId,
            String name,
            Subscription.SubscriptionInterval interval,
            Integer intervalCount,
            Long amount,
            String currency,
            Integer trialDays
    ) {}
}
