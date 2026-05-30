package com.nexuspay.domain.valueobject;

import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    
    @Test
    void shouldCreateMoney() {
        Money money = Money.of(1000, "USD");
        assertEquals(BigInteger.valueOf(1000), money.amount());
        assertEquals("USD", money.currency().getCurrencyCode());
    }
    
    @Test
    void shouldAddMoney() {
        Money a = Money.of(1000, "USD");
        Money b = Money.of(500, "USD");
        Money result = a.add(b);
        assertEquals(BigInteger.valueOf(1500), result.amount());
    }
    
    @Test
    void shouldNotAddDifferentCurrencies() {
        Money a = Money.of(1000, "USD");
        Money b = Money.of(500, "EUR");
        assertThrows(IllegalArgumentException.class, () -> a.add(b));
    }
    
    @Test
    void shouldCompareMoney() {
        Money a = Money.of(1000, "USD");
        Money b = Money.of(500, "USD");
        assertTrue(a.isGreaterThan(b));
        assertFalse(b.isGreaterThan(a));
    }
}
