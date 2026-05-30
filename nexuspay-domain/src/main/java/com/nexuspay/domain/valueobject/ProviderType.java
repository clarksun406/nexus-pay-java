package com.nexuspay.domain.valueobject;

public enum ProviderType {
    STRIPE, SQUARE, BRAINTREE;
    
    public String getPaymentIdPrefix() {
        return switch (this) {
            case STRIPE -> "pi_";
            case SQUARE -> "sq_";
            case BRAINTREE -> "bt_";
        };
    }
}
