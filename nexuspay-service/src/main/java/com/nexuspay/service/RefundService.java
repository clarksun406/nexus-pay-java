package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.Refund;
import com.nexuspay.repository.PaymentIntentRepository;
import com.nexuspay.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {
    
    private final RefundRepository refundRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    
    @Transactional
    public Refund create(UUID merchantId, CreateRefundRequest req) {
        PaymentIntent intent = paymentIntentRepository.findById(req.paymentIntentId())
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND));
        
        if (!intent.getMerchantId().equals(merchantId)) {
            throw new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND);
        }
        
        if (intent.getStatus() != PaymentIntent.PaymentStatus.SUCCEEDED) {
            throw new BusinessException("Payment not succeeded", HttpStatus.BAD_REQUEST);
        }
        
        BigInteger refundAmount = req.amount() != null ? req.amount() : intent.getAmount();
        
        Refund refund = new Refund();
        refund.setPaymentIntentId(intent.getId());
        refund.setMerchantId(merchantId);
        refund.setMode(intent.getMode());
        refund.setAmount(refundAmount);
        refund.setCurrency(intent.getCurrency());
        refund.setReason(req.reason());
        refund.setStatus(Refund.RefundStatus.PENDING);
        
        // Mock: Call provider API for refund
        refund.setStatus(Refund.RefundStatus.SUCCEEDED);
        refund.setProviderRefundId("re_" + UUID.randomUUID().toString().replace("-", ""));
        
        return refundRepository.save(refund);
    }
    
    public List<Refund> listRefunds(UUID merchantId) {
        return refundRepository.findByMerchantId(merchantId);
    }
    
    public Refund getRefund(UUID refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found", HttpStatus.NOT_FOUND));
    }
    
    public record CreateRefundRequest(UUID paymentIntentId, BigInteger amount, Refund.RefundReason reason) {}
}
