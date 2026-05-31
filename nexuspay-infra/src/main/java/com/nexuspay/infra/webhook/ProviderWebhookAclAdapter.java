package com.nexuspay.infra.webhook;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.nexuspay.service.provider.ProviderWebhookAclPort;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
public class ProviderWebhookAclAdapter implements ProviderWebhookAclPort {

    @Value("${webhooks.stripe.signing-secret:}")
    private String stripeSigningSecret;

    @Value("${webhooks.square.signature-key:}")
    private String squareSignatureKey;

    @Value("${webhooks.braintree.environment:sandbox}")
    private String braintreeEnvironment;

    @Value("${webhooks.braintree.merchant-id:}")
    private String braintreeMerchantId;

    @Value("${webhooks.braintree.public-key:}")
    private String braintreePublicKey;

    @Value("${webhooks.braintree.private-key:}")
    private String braintreePrivateKey;

    @Override
    public boolean verifyStripe(String payload, String stripeSignatureHeader) {
        if (isBlank(stripeSigningSecret)) {
            log.warn("Stripe webhook signing secret is not configured; reject by default");
            return false;
        }
        try {
            Webhook.constructEvent(payload, stripeSignatureHeader, stripeSigningSecret);
            return true;
        } catch (Exception e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifySquare(String payload, String squareSignatureHeader, String notificationUrl) {
        if (isBlank(squareSignatureKey)) {
            log.warn("Square webhook signature key is not configured; reject by default");
            return false;
        }
        if (isBlank(squareSignatureHeader) || notificationUrl == null) {
            return false;
        }
        try {
            String body = payload == null ? "" : payload;
            String signedMessage = notificationUrl + body;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(squareSignatureKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(signedMessage.getBytes(StandardCharsets.UTF_8));
            String expected = Base64.getEncoder().encodeToString(digest);

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    squareSignatureHeader.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.warn("Square webhook signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String generateBraintreeVerification(String challenge) {
        if (isBlank(challenge)) return null;
        if (isBlank(braintreeMerchantId) || isBlank(braintreePublicKey) || isBlank(braintreePrivateKey)) {
            log.warn("Braintree credentials are not configured; cannot generate verification");
            return null;
        }

        try {
            Environment env = "production".equalsIgnoreCase(braintreeEnvironment)
                    ? Environment.PRODUCTION
                    : Environment.SANDBOX;

            BraintreeGateway gateway = new BraintreeGateway(
                    env,
                    braintreeMerchantId,
                    braintreePublicKey,
                    braintreePrivateKey
            );

            return gateway.webhookNotification().verify(challenge);
        } catch (Exception e) {
            log.warn("Braintree challenge generation failed: {}", e.getMessage());
            return null;
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}