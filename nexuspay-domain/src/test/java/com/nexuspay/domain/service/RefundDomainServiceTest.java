package com.nexuspay.domain.service;

import com.nexuspay.domain.valueobject.PaymentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class RefundDomainServiceTest {

    private final RefundDomainService service = new RefundDomainService();

    @Test
    void shouldValidateRefundablePayment() {
        assertDoesNotThrow(() ->
                service.validateRefundable(PaymentStatus.SUCCEEDED, "pi_123"));
    }

    @Test
    void shouldRejectNonSucceededPayment() {
        assertThrows(IllegalStateException.class, () ->
                service.validateRefundable(PaymentStatus.PROCESSING, "pi_123"));
        assertThrows(IllegalStateException.class, () ->
                service.validateRefundable(PaymentStatus.FAILED, "pi_123"));
    }

    @Test
    void shouldRejectRefundWithoutProviderPaymentId() {
        assertThrows(IllegalStateException.class, () ->
                service.validateRefundable(PaymentStatus.SUCCEEDED, null));
    }

    @Test
    void shouldValidateFullRefundAmount() {
        BigInteger amount = service.validateRefundAmount(null, BigInteger.valueOf(1000));
        assertEquals(BigInteger.valueOf(1000), amount);
    }

    @Test
    void shouldValidatePartialRefundAmount() {
        BigInteger amount = service.validateRefundAmount(BigInteger.valueOf(500), BigInteger.valueOf(1000));
        assertEquals(BigInteger.valueOf(500), amount);
    }

    @Test
    void shouldRejectZeroRefundAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                service.validateRefundAmount(BigInteger.ZERO, BigInteger.valueOf(1000)));
    }

    @Test
    void shouldRejectNegativeRefundAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                service.validateRefundAmount(BigInteger.valueOf(-100), BigInteger.valueOf(1000)));
    }

    @Test
    void shouldRejectRefundExceedingOriginal() {
        assertThrows(IllegalArgumentException.class, () ->
                service.validateRefundAmount(BigInteger.valueOf(2000), BigInteger.valueOf(1000)));
    }
}