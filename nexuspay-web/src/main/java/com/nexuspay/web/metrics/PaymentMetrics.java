package com.nexuspay.web.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Application-level metrics for payment operations.
 * Exposed via /actuator/prometheus for Prometheus scraping.
 */
@Component
public class PaymentMetrics {

    private final Counter paymentIntentsCreated;
    private final Counter paymentIntentsSucceeded;
    private final Counter paymentIntentsFailed;
    private final Counter refundsCreated;
    private final Counter refundsSucceeded;
    private final Counter webhooksDelivered;
    private final Counter webhooksFailed;
    private final Timer paymentProcessingTime;
    private final Timer refundProcessingTime;

    public PaymentMetrics(MeterRegistry registry) {
        this.paymentIntentsCreated = Counter.builder("nexuspay.payments.created")
                .description("Total payment intents created")
                .register(registry);
        this.paymentIntentsSucceeded = Counter.builder("nexuspay.payments.succeeded")
                .description("Total payment intents succeeded")
                .register(registry);
        this.paymentIntentsFailed = Counter.builder("nexuspay.payments.failed")
                .description("Total payment intents failed")
                .register(registry);
        this.refundsCreated = Counter.builder("nexuspay.refunds.created")
                .description("Total refunds created")
                .register(registry);
        this.refundsSucceeded = Counter.builder("nexuspay.refunds.succeeded")
                .description("Total refunds succeeded")
                .register(registry);
        this.webhooksDelivered = Counter.builder("nexuspay.webhooks.delivered")
                .description("Total webhooks delivered successfully")
                .register(registry);
        this.webhooksFailed = Counter.builder("nexuspay.webhooks.failed")
                .description("Total webhook delivery failures")
                .register(registry);
        this.paymentProcessingTime = Timer.builder("nexuspay.payments.processing_time")
                .description("Payment processing time")
                .register(registry);
        this.refundProcessingTime = Timer.builder("nexuspay.refunds.processing_time")
                .description("Refund processing time")
                .register(registry);
    }

    public void recordPaymentCreated() {
        paymentIntentsCreated.increment();
    }

    public void recordPaymentSucceeded() {
        paymentIntentsSucceeded.increment();
    }

    public void recordPaymentFailed() {
        paymentIntentsFailed.increment();
    }

    public void recordRefundCreated() {
        refundsCreated.increment();
    }

    public void recordRefundSucceeded() {
        refundsSucceeded.increment();
    }

    public void recordWebhookDelivered() {
        webhooksDelivered.increment();
    }

    public void recordWebhookFailed() {
        webhooksFailed.increment();
    }

    public void recordPaymentProcessingTime(long durationMs) {
        paymentProcessingTime.record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordRefundProcessingTime(long durationMs) {
        refundProcessingTime.record(durationMs, TimeUnit.MILLISECONDS);
    }
}