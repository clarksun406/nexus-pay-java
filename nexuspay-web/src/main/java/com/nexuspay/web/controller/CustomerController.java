package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.Customer;
import com.nexuspay.domain.entity.PaymentMethod;
import com.nexuspay.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @PostMapping
    public ResponseEntity<Customer> create(
            @PathVariable UUID merchantId,
            @RequestBody CustomerService.CreateRequest req) {
        return ResponseEntity.ok(customerService.create(merchantId, req));
    }
    
    @GetMapping
    public ResponseEntity<List<Customer>> list(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(customerService.listCustomers(merchantId));
    }
    
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> get(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.getCustomer(merchantId, customerId));
    }
    
    @PutMapping("/{customerId}")
    public ResponseEntity<Customer> update(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId,
            @RequestBody CustomerService.UpdateRequest req) {
        return ResponseEntity.ok(customerService.update(merchantId, customerId, req));
    }
    
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId) {
        customerService.delete(merchantId, customerId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{customerId}/payment-methods")
    public ResponseEntity<PaymentMethod> addPaymentMethod(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId,
            @RequestBody CustomerService.PaymentMethodRequest req) {
        return ResponseEntity.ok(customerService.addPaymentMethod(merchantId, customerId, req));
    }
    
    @GetMapping("/{customerId}/payment-methods")
    public ResponseEntity<List<PaymentMethod>> listPaymentMethods(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(customerService.listPaymentMethods(merchantId, customerId));
    }
    
    @DeleteMapping("/{customerId}/payment-methods/{paymentMethodId}")
    public ResponseEntity<Void> removePaymentMethod(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId,
            @PathVariable UUID paymentMethodId) {
        customerService.removePaymentMethod(merchantId, customerId, paymentMethodId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{customerId}/payment-methods/{paymentMethodId}/default")
    public ResponseEntity<PaymentMethod> setDefaultPaymentMethod(
            @PathVariable UUID merchantId,
            @PathVariable UUID customerId,
            @PathVariable UUID paymentMethodId) {
        return ResponseEntity.ok(customerService.setDefaultPaymentMethod(merchantId, customerId, paymentMethodId));
    }
}
