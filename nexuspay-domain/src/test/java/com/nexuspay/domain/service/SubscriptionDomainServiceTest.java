package com.nexuspay.domain.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionDomainServiceTest {

    private final SubscriptionDomainService service = new SubscriptionDomainService();

    @Test
    void shouldCalculateDayPeriod() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        var period = service.calculateNextPeriod(now, "DAY", 1);
        assertEquals(now, period.start());
        assertEquals(Instant.parse("2026-01-02T00:00:00Z"), period.end());
    }

    @Test
    void shouldCalculateWeekPeriod() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        var period = service.calculateNextPeriod(now, "WEEK", 1);
        assertEquals(now, period.start());
        assertEquals(Instant.parse("2026-01-08T00:00:00Z"), period.end());
    }

    @Test
    void shouldCalculateMonthPeriod() {
        Instant now = Instant.parse("2026-01-15T00:00:00Z");
        var period = service.calculateNextPeriod(now, "MONTH", 1);
        assertEquals(now, period.start());
        assertEquals(Instant.parse("2026-02-15T00:00:00Z"), period.end());
    }

    @Test
    void shouldCalculateYearPeriod() {
        Instant now = Instant.parse("2026-06-06T00:00:00Z");
        var period = service.calculateNextPeriod(now, "YEAR", 1);
        assertEquals(now, period.start());
        assertEquals(Instant.parse("2027-06-06T00:00:00Z"), period.end());
    }

    @Test
    void shouldHandleIntervalCount() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        var period = service.calculateNextPeriod(now, "MONTH", 3);
        assertEquals(Instant.parse("2026-04-01T00:00:00Z"), period.end());
    }

    @Test
    void shouldRejectUnknownInterval() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateNextPeriod(Instant.now(), "UNKNOWN", 1));
    }
}