package com.nexuspay.domain.valueobject;

public enum PaymentStatus {
    REQUIRES_PAYMENT_METHOD,
    REQUIRES_CONFIRMATION,
    REQUIRES_ACTION,
    PROCESSING,
    REQUIRES_CAPTURE,
    CANCELED,
    SUCCEEDED,
    FAILED;
    
    public boolean canConfirm() {
        return this == REQUIRES_PAYMENT_METHOD;
    }
    
    public boolean canCapture() {
        return this == REQUIRES_CAPTURE;
    }
    
    public boolean canCancel() {
        return this != SUCCEEDED && this != CANCELED;
    }
    
    public boolean isTerminal() {
        return this == SUCCEEDED || this == FAILED || this == CANCELED;
    }
}
