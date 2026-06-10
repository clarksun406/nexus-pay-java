package com.nexuspay.web.controller;

import com.nexuspay.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<?> listInvoices(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(invoiceService.listByMerchant(merchantId));
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoice(@PathVariable UUID merchantId, @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.getInvoice(merchantId, invoiceId));
    }

    @PostMapping("/{invoiceId}/void")
    public ResponseEntity<?> voidInvoice(@PathVariable UUID merchantId, @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.voidInvoice(merchantId, invoiceId));
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<?> listCustomerInvoices(@PathVariable UUID merchantId, @PathVariable UUID customerId) {
        return ResponseEntity.ok(invoiceService.listByCustomer(merchantId, customerId));
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<?> listSubscriptionInvoices(@PathVariable UUID merchantId, @PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(invoiceService.listBySubscription(merchantId, subscriptionId));
    }
}
