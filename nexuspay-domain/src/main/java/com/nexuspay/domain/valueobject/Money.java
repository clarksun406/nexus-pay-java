package com.nexuspay.domain.valueobject;

import java.math.BigInteger;
import java.util.Currency;

public record Money(BigInteger amount, Currency currency) {
    
    public Money {
        if (amount == null || amount.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
    }
    
    public static Money of(long amount, String currencyCode) {
        return new Money(BigInteger.valueOf(amount), Currency.getInstance(currencyCode));
    }
    
    public static Money of(BigInteger amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode.toUpperCase()));
    }
    
    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }
    
    public Money subtract(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(amount.subtract(other.amount), currency);
    }
    
    public boolean isGreaterThan(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money with different currencies");
        }
        return amount.compareTo(other.amount) > 0;
    }
}
