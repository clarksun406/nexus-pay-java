package com.nexuspay.service.provider;

/**
 * Port for inbound provider webhook verification/challenge handling.
 * Implemented in infrastructure adapters.
 */
public interface ProviderWebhookAclPort {

    boolean verifyStripe(String payload, String stripeSignatureHeader);

    boolean verifySquare(String payload, String squareSignatureHeader, String notificationUrl);

    String generateBraintreeVerification(String challenge);
}