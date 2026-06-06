package com.nexuspay.domain.service;

import com.nexuspay.domain.valueobject.Money;
import com.nexuspay.domain.valueobject.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

/**
 * Domain service for refund business rules.
 */
@Service
public class RefundDomainService {

    /**
     * Validate that a refund can be created for the given payment.
     *
     * @throws IllegalStateException if the payment is not in a refundable state
     */
    public void validateRefundable(PaymentStatus paymentStatus, String providerPaymentId) {
        if (paymentStatus != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Payment not succeeded, cannot refund");
        }
        if (providerPaymentId == null) {
            throw new IllegalStateException("Payment has no provider charge to refund");
        }
    }

    /**
     * Validate and normalize the refund amount.
     *
     * @param requestedAmount the requested refund amount (may be null for full refund)
     * @param originalAmount  the original payment amount
     * @return the validated refund amount
     * @throws IllegalArgumentException if the amount is invalid
     */
    public BigInteger validateRefundAmount(BigInteger requestedAmount, BigInteger originalAmount) {
        BigInteger amount = requestedAmount != null ? requestedAmount : originalAmount;
        if (amount.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        if (amount.compareTo(originalAmount) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds original payment amount");
        }
        return amount;
    }
}