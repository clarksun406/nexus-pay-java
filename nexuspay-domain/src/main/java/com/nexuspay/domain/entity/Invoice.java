package com.nexuspay.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "payment_intent_id")
    private UUID paymentIntentId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false)
    private BigInteger amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "tax_amount")
    private BigInteger taxAmount;

    @Column(name = "total_amount", nullable = false)
    private BigInteger totalAmount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "line_items", columnDefinition = "TEXT")
    private String lineItems;

    @Column(name = "billing_reason", length = 50)
    private String billingReason;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "period_start")
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum InvoiceStatus {
        DRAFT, OPEN, PAID, VOID, UNCOLLECTIBLE
    }
}