package com.nexuspay.domain.valueobject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {
    
    @Test
    void canConfirmWhenRequiresPaymentMethod() {
        assertTrue(PaymentStatus.REQUIRES_PAYMENT_METHOD.canConfirm());
    }
    
    @Test
    void cannotConfirmWhenProcessing() {
        assertFalse(PaymentStatus.PROCESSING.canConfirm());
    }
    
    @Test
    void canCaptureWhenRequiresCapture() {
        assertTrue(PaymentStatus.REQUIRES_CAPTURE.canCapture());
    }
    
    @Test
    void canCancelWhenProcessing() {
        assertTrue(PaymentStatus.PROCESSING.canCancel());
    }
    
    @Test
    void cannotCancelWhenSucceeded() {
        assertFalse(PaymentStatus.SUCCEEDED.canCancel());
    }
    
    @Test
    void terminalStatesAreTerminal() {
        assertTrue(PaymentStatus.SUCCEEDED.isTerminal());
        assertTrue(PaymentStatus.FAILED.isTerminal());
        assertTrue(PaymentStatus.CANCELED.isTerminal());
        assertFalse(PaymentStatus.PROCESSING.isTerminal());
    }
}
