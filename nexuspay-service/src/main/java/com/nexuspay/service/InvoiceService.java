package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Invoice;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.Subscription;
import com.nexuspay.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createFromSubscription(Subscription subscription, PaymentIntent paymentIntent, String billingReason) {
        Invoice invoice = new Invoice();
        invoice.setMerchantId(subscription.getMerchantId());
        invoice.setCustomerId(subscription.getCustomerId());
        invoice.setSubscriptionId(subscription.getId());
        invoice.setPaymentIntentId(paymentIntent.getId());
        invoice.setInvoiceNumber(generateInvoiceNumber(subscription.getMerchantId()));
        invoice.setStatus(Invoice.InvoiceStatus.OPEN);
        invoice.setAmount(BigInteger.valueOf(subscription.getAmount()));
        invoice.setCurrency(subscription.getCurrency());
        invoice.setTotalAmount(BigInteger.valueOf(subscription.getAmount()));
        invoice.setBillingReason(billingReason);
        invoice.setPeriodStart(subscription.getCurrentPeriodStart());
        invoice.setPeriodEnd(subscription.getCurrentPeriodEnd());
        invoice.setDueDate(Instant.now().plus(30, ChronoUnit.DAYS));
        invoice.setDescription(subscription.getName() + " - " + billingReason);

        if (paymentIntent.getStatus() == PaymentIntent.PaymentStatus.SUCCEEDED) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice markPaid(UUID invoiceId, UUID paymentIntentId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Invoice not found", HttpStatus.NOT_FOUND));
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        invoice.setPaymentIntentId(paymentIntentId);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice voidInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Invoice not found", HttpStatus.NOT_FOUND));
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessException("Cannot void paid invoice", HttpStatus.BAD_REQUEST);
        }
        invoice.setStatus(Invoice.InvoiceStatus.VOID);
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> listByMerchant(UUID merchantId) {
        return invoiceRepository.findByMerchantId(merchantId);
    }

    public List<Invoice> listByCustomer(UUID customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    public List<Invoice> listBySubscription(UUID subscriptionId) {
        return invoiceRepository.findBySubscriptionId(subscriptionId);
    }

    public Invoice getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Invoice not found", HttpStatus.NOT_FOUND));
    }

    private String generateInvoiceNumber(UUID merchantId) {
        String shortId = merchantId.toString().substring(0, 8);
        long seq = System.currentTimeMillis() % 100000;
        return String.format("INV-%s-%05d", shortId, seq);
    }
}