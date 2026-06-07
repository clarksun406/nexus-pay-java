package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Invoice;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.Subscription;
import com.nexuspay.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepo;
    @InjectMocks private InvoiceService invoiceService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void shouldCreateInvoiceFromSubscription() {
        Subscription sub = new Subscription();
        sub.setId(UUID.randomUUID());
        sub.setMerchantId(UUID.randomUUID());
        sub.setCustomerId(UUID.randomUUID());
        sub.setAmount(2999L);
        sub.setCurrency("usd");
        sub.setName("Pro Plan");
        sub.setCurrentPeriodStart(Instant.now());
        sub.setCurrentPeriodEnd(Instant.now().plusSeconds(2592000));

        PaymentIntent intent = new PaymentIntent();
        intent.setId(UUID.randomUUID());
        intent.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);

        when(invoiceRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invoice invoice = invoiceService.createFromSubscription(sub, intent, "subscription_cycle");

        assertNotNull(invoice);
        assertEquals(Invoice.InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(BigInteger.valueOf(2999), invoice.getAmount());
        verify(invoiceRepo).save(any());
    }

    @Test
    void shouldMarkInvoiceAsPaid() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setStatus(Invoice.InvoiceStatus.OPEN);

        when(invoiceRepo.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepo.save(any())).thenReturn(invoice);

        Invoice result = invoiceService.markPaid(invoiceId, UUID.randomUUID());

        assertEquals(Invoice.InvoiceStatus.PAID, result.getStatus());
        assertNotNull(result.getPaidAt());
    }

    @Test
    void shouldVoidInvoice() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setStatus(Invoice.InvoiceStatus.OPEN);

        when(invoiceRepo.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(invoiceRepo.save(any())).thenReturn(invoice);

        Invoice result = invoiceService.voidInvoice(invoiceId);

        assertEquals(Invoice.InvoiceStatus.VOID, result.getStatus());
    }

    @Test
    void shouldNotVoidPaidInvoice() {
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setStatus(Invoice.InvoiceStatus.PAID);

        when(invoiceRepo.findById(invoiceId)).thenReturn(Optional.of(invoice));

        assertThrows(BusinessException.class, () -> invoiceService.voidInvoice(invoiceId));
    }

    @Test
    void shouldListInvoicesByMerchant() {
        UUID merchantId = UUID.randomUUID();
        when(invoiceRepo.findByMerchantId(merchantId)).thenReturn(List.of(new Invoice()));

        List<Invoice> invoices = invoiceService.listByMerchant(merchantId);
        assertEquals(1, invoices.size());
    }
}