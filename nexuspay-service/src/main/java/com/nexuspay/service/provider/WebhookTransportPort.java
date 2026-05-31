package com.nexuspay.service.provider;

import java.util.Map;

/**
 * Port for outbound webhook transport.
 * Infrastructure adapters should implement network transport details.
 */
public interface WebhookTransportPort {
    void postJson(String url, Object payload, Map<String, String> headers);
}