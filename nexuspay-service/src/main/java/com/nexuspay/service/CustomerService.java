package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.Customer;
import com.nexuspay.domain.entity.PaymentMethod;
import com.nexuspay.repository.CustomerRepository;
import com.nexuspay.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    
    @Transactional
    public Customer create(UUID merchantId, CreateRequest req) {
        if (req.email() != null && customerRepository.existsByMerchantIdAndEmail(merchantId, req.email())) {
            throw new BusinessException("Customer with this email already exists", HttpStatus.CONFLICT);
        }
        
        Customer customer = new Customer();
        customer.setMerchantId(merchantId);
        customer.setEmail(req.email());
        customer.setName(req.name());
        customer.setPhone(req.phone());
        
        return customerRepository.save(customer);
    }
    
    public Customer getCustomer(UUID merchantId, UUID customerId) {
        return customerRepository.findByMerchantIdAndId(merchantId, customerId)
                .orElseThrow(() -> new BusinessException("Customer not found", HttpStatus.NOT_FOUND));
    }
    
    public List<Customer> listCustomers(UUID merchantId) {
        return customerRepository.findByMerchantId(merchantId);
    }
    
    @Transactional
    public Customer update(UUID merchantId, UUID customerId, UpdateRequest req) {
        Customer customer = getCustomer(merchantId, customerId);
        
        if (req.email() != null) customer.setEmail(req.email());
        if (req.name() != null) customer.setName(req.name());
        if (req.phone() != null) customer.setPhone(req.phone());
        
        return customerRepository.save(customer);
    }
    
    @Transactional
    public void delete(UUID merchantId, UUID customerId) {
        Customer customer = getCustomer(merchantId, customerId);
        customer.setStatus(Customer.CustomerStatus.DELETED);
        customerRepository.save(customer);
    }
    
    @Transactional
    public PaymentMethod addPaymentMethod(UUID merchantId, UUID customerId, PaymentMethodRequest req) {
        Customer customer = getCustomer(merchantId, customerId);
        
        // Check if this should be the default payment method
        boolean isDefault = paymentMethodRepository.findByCustomerIdAndStatus(
                customerId, PaymentMethod.PaymentMethodStatus.ACTIVE).isEmpty();
        
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setCustomerId(customerId);
        paymentMethod.setMerchantId(merchantId);
        paymentMethod.setProviderPaymentMethodId(req.providerPaymentMethodId());
        paymentMethod.setType(req.type());
        paymentMethod.setLast4(req.last4());
        paymentMethod.setBrand(req.brand());
        paymentMethod.setExpiryMonth(req.expiryMonth());
        paymentMethod.setExpiryYear(req.expiryYear());
        paymentMethod.setCardHolderName(req.cardHolderName());
        paymentMethod.setIsDefault(isDefault);
        
        return paymentMethodRepository.save(paymentMethod);
    }
    
    public List<PaymentMethod> listPaymentMethods(UUID merchantId, UUID customerId) {
        getCustomer(merchantId, customerId);
        return paymentMethodRepository.findByCustomerIdAndStatus(customerId, PaymentMethod.PaymentMethodStatus.ACTIVE);
    }
    
    @Transactional
    public void removePaymentMethod(UUID merchantId, UUID customerId, UUID paymentMethodId) {
        PaymentMethod pm = paymentMethodRepository.findByCustomerIdAndId(customerId, paymentMethodId)
                .orElseThrow(() -> new BusinessException("Payment method not found", HttpStatus.NOT_FOUND));
        
        pm.setStatus(PaymentMethod.PaymentMethodStatus.INACTIVE);
        paymentMethodRepository.save(pm);
    }
    
    @Transactional
    public PaymentMethod setDefaultPaymentMethod(UUID merchantId, UUID customerId, UUID paymentMethodId) {
        List<PaymentMethod> methods = paymentMethodRepository.findByCustomerId(customerId);
        
        for (PaymentMethod m : methods) {
            m.setIsDefault(m.getId().equals(paymentMethodId));
        }
        
        paymentMethodRepository.saveAll(methods);
        
        return methods.stream()
                .filter(m -> m.getId().equals(paymentMethodId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Payment method not found", HttpStatus.NOT_FOUND));
    }
    
    public record CreateRequest(String email, String name, String phone) {}
    public record UpdateRequest(String email, String name, String phone) {}
    public record PaymentMethodRequest(
            String providerPaymentMethodId,
            PaymentMethod.PaymentMethodType type,
            String last4,
            String brand,
            Integer expiryMonth,
            Integer expiryYear,
            String cardHolderName
    ) {}
}
