package com.nexuspay.domain.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Domain service for subscription period calculation.
 * Pure domain logic with no infrastructure dependencies.
 */
@Service
public class SubscriptionDomainService {

    public record Period(Instant start, Instant end) {}

    public Period calculateNextPeriod(Instant now, String interval, int intervalCount) {
        ZonedDateTime current = now.atZone(ZoneOffset.UTC);
        ZonedDateTime next = switch (interval.toUpperCase()) {
            case "DAY" -> current.plusDays(intervalCount);
            case "WEEK" -> current.plusWeeks(intervalCount);
            case "MONTH" -> current.plusMonths(intervalCount);
            case "YEAR" -> current.plusYears(intervalCount);
            default -> throw new IllegalArgumentException("Unknown interval: " + interval);
        };
        return new Period(now, next.toInstant());
    }
}