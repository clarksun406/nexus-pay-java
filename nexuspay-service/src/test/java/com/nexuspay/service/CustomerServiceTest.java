package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Customer;
import com.nexuspay.repository.CustomerRepository;
import com.nexuspay.repository.PaymentMethodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock private CustomerRepository customerRepo;
    @Mock private PaymentMethodRepository paymentMethodRepo;
    @InjectMocks private CustomerService customerService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void shouldCreateCustomer() {
        UUID merchantId = UUID.randomUUID();
        when(customerRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.create(merchantId,
                new CustomerService.CreateRequest("test@example.com", "Test Customer", "1234567890"));

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(customerRepo).save(any());
    }

    @Test
    void shouldListCustomers() {
        UUID merchantId = UUID.randomUUID();
        when(customerRepo.findByMerchantId(merchantId)).thenReturn(List.of(new Customer()));

        List<Customer> customers = customerService.listCustomers(merchantId);
        assertEquals(1, customers.size());
    }

    @Test
    void shouldGetCustomer() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepo.findByMerchantIdAndId(merchantId, customerId)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomer(merchantId, customerId);
        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(customerRepo.findByMerchantIdAndId(merchantId, customerId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> customerService.getCustomer(merchantId, customerId));
    }

    @Test
    void shouldDeleteCustomer() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);

        when(customerRepo.findByMerchantIdAndId(merchantId, customerId)).thenReturn(Optional.of(customer));

        customerService.delete(merchantId, customerId);
        verify(customerRepo).save(any()); // Soft delete uses save()
    }
}