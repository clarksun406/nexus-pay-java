package com.nexuspay.web.integration;

import com.nexuspay.domain.entity.*;
import com.nexuspay.repository.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Disabled("Requires Docker - run with Docker available for integration testing")
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("nexuspay_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private PaymentIntentJpaRepository paymentIntentRepo;
    @Autowired private RefundRepository refundRepo;
    @Autowired private InvoiceRepository invoiceRepo;
    @Autowired private SubscriptionRepository subscriptionRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private VaultEntryRepository vaultRepo;
    @Autowired private WebhookEndpointRepository webhookEndpointRepo;
    @Autowired private OutboxEventRepository outboxEventRepo;
    @Autowired private MerchantRepository merchantRepo;
    @Autowired private OrganizationRepository organizationRepo;
    @Autowired private ApiKeyRepository apiKeyRepo;

    // ---- PaymentIntent ----

    @Test
    void shouldSaveAndFindPaymentIntent() {
        UUID merchantId = UUID.randomUUID();
        PaymentIntent intent = new PaymentIntent();
        intent.setMerchantId(merchantId);
        intent.setAmount(BigInteger.valueOf(2000));
        intent.setCurrency("usd");
        intent.setStatus(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD);
        intent.setIdempotencyKey("idem_001");
        intent.setMode(PaymentIntent.Mode.TEST);
        intent.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);

        PaymentIntent saved = paymentIntentRepo.save(intent);
        assertNotNull(saved.getId());

        Optional<PaymentIntent> found = paymentIntentRepo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(BigInteger.valueOf(2000), found.get().getAmount());
        assertEquals("usd", found.get().getCurrency());
    }

    @Test
    void shouldFindPaymentIntentsByMerchant() {
        UUID merchantA = UUID.randomUUID();
        UUID merchantB = UUID.randomUUID();

        PaymentIntent intentA = new PaymentIntent();
        intentA.setMerchantId(merchantA);
        intentA.setAmount(BigInteger.valueOf(1000));
        intentA.setCurrency("usd");
        intentA.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);
        intentA.setMode(PaymentIntent.Mode.TEST);
        intentA.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);
        paymentIntentRepo.save(intentA);

        PaymentIntent intentB = new PaymentIntent();
        intentB.setMerchantId(merchantB);
        intentB.setAmount(BigInteger.valueOf(500));
        intentB.setCurrency("eur");
        intentB.setStatus(PaymentIntent.PaymentStatus.PROCESSING);
        intentB.setMode(PaymentIntent.Mode.TEST);
        intentB.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);
        paymentIntentRepo.save(intentB);

        List<PaymentIntent> merchantAIntents = paymentIntentRepo.findByMerchantId(merchantA);
        assertEquals(1, merchantAIntents.size());
        assertEquals(BigInteger.valueOf(1000), merchantAIntents.get(0).getAmount());
    }

    // ---- Refund ----

    @Test
    void shouldSaveAndFindRefund() {
        UUID merchantId = UUID.randomUUID();
        PaymentIntent intent = createAndSaveIntent(merchantId, "pi_refund_test");

        Refund refund = new Refund();
        refund.setPaymentIntentId(intent.getId());
        refund.setMerchantId(merchantId);
        refund.setAmount(BigInteger.valueOf(500));
        refund.setCurrency("usd");
        refund.setStatus(Refund.RefundStatus.SUCCEEDED);
        refund.setProviderRefundId("re_test_001");

        Refund saved = refundRepo.save(refund);
        assertNotNull(saved.getId());

        List<Refund> refunds = refundRepo.findByPaymentIntentId(intent.getId());
        assertEquals(1, refunds.size());
        assertEquals("re_test_001", refunds.get(0).getProviderRefundId());
    }

    // ---- Invoice ----

    @Test
    void shouldSaveAndFindInvoice() {
        UUID merchantId = UUID.randomUUID();

        Invoice invoice = new Invoice();
        invoice.setMerchantId(merchantId);
        invoice.setAmount(BigInteger.valueOf(3000));
        invoice.setCurrency("usd");
        invoice.setTotalAmount(BigInteger.valueOf(3000));
        invoice.setInvoiceNumber("INV-TEST-001");
        invoice.setStatus(Invoice.InvoiceStatus.OPEN);
        invoice.setDueDate(Instant.now().plusSeconds(86400));

        Invoice saved = invoiceRepo.save(invoice);
        assertNotNull(saved.getId());

        List<Invoice> merchantInvoices = invoiceRepo.findByMerchantId(merchantId);
        assertEquals(1, merchantInvoices.size());
    }

    // ---- Subscription ----

    @Test
    void shouldSaveAndFindSubscription() {
        UUID merchantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        Subscription sub = new Subscription();
        sub.setMerchantId(merchantId);
        sub.setCustomerId(customerId);
        sub.setName("Pro Plan");
        sub.setAmount(2999L);
        sub.setCurrency("usd");
        sub.setInterval(Subscription.SubscriptionInterval.MONTH);
        sub.setIntervalCount(1);
        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodStart(Instant.now());
        sub.setCurrentPeriodEnd(Instant.now().plusSeconds(2592000));

        Subscription saved = subscriptionRepo.save(sub);
        assertNotNull(saved.getId());

        List<Subscription> dueSubs = subscriptionRepo.findDueForRenewal(Instant.now().plusSeconds(2592000));
        assertFalse(dueSubs.isEmpty());
    }

    // ---- Customer ----

    @Test
    void shouldSaveAndFindCustomer() {
        UUID merchantId = UUID.randomUUID();

        Customer customer = new Customer();
        customer.setMerchantId(merchantId);
        customer.setEmail("test@example.com");
        customer.setName("Test Customer");

        Customer saved = customerRepo.save(customer);
        assertNotNull(saved.getId());

        List<Customer> customers = customerRepo.findByMerchantId(merchantId);
        assertEquals(1, customers.size());
    }

    // ---- Vault ----

    @Test
    void shouldSaveAndFindVaultEntry() {
        UUID merchantId = UUID.randomUUID();

        VaultEntry entry = new VaultEntry();
        entry.setMerchantId(merchantId);
        entry.setToken("vault_tok_test_001");
        entry.setTokenHash("hash_of_token");
        entry.setEntryType(VaultEntry.EntryType.CARD);
        entry.setEncryptedData("encrypted_card_data");
        entry.setFingerprint("fp_test_001");
        entry.setStatus(VaultEntry.EntryStatus.ACTIVE);

        VaultEntry saved = vaultRepo.save(entry);
        assertNotNull(saved.getId());

        Optional<VaultEntry> found = vaultRepo.findByTokenHash("hash_of_token");
        assertTrue(found.isPresent());
        assertEquals(VaultEntry.EntryType.CARD, found.get().getEntryType());
    }

    // ---- Webhook Endpoint ----

    @Test
    void shouldSaveAndFindWebhookEndpoint() {
        UUID merchantId = UUID.randomUUID();

        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setMerchantId(merchantId);
        endpoint.setUrl("https://example.com/webhook");
        endpoint.setSigningSecret("whsec_test");

        WebhookEndpoint saved = webhookEndpointRepo.save(endpoint);
        assertNotNull(saved.getId());

        List<WebhookEndpoint> endpoints = webhookEndpointRepo.findByMerchantId(merchantId);
        assertEquals(1, endpoints.size());
    }

    // ---- Outbox ----

    @Test
    void shouldSaveAndFindOutboxEvent() {
        UUID merchantId = UUID.randomUUID();

        OutboxEvent event = new OutboxEvent();
        event.setEventType("payment_intent.succeeded");
        event.setPayload("{\"id\":\"pi_test\"}");
        event.setStatus(OutboxEvent.EventStatus.PENDING);
        event.setRetryCount(0);

        OutboxEvent saved = outboxEventRepo.save(event);
        assertNotNull(saved.getId());

        List<OutboxEvent> pending = outboxEventRepo.findByStatusOrderByCreatedAtAsc(OutboxEvent.EventStatus.PENDING);
        assertFalse(pending.isEmpty());
    }

    // ---- Merchant + Organization ----

    @Test
    void shouldSaveAndFindMerchantWithOrganization() {
        Organization org = new Organization();
        org.setName("Test Org");
        Organization savedOrg = organizationRepo.save(org);

        Merchant merchant = new Merchant();
        merchant.setOrganizationId(savedOrg.getId());
        merchant.setName("Test Merchant");
        merchant.setStatus(Merchant.MerchantStatus.ACTIVE);
        Merchant saved = merchantRepo.save(merchant);

        assertNotNull(saved.getId());
        assertEquals(savedOrg.getId(), saved.getOrganizationId());
    }

    // ---- ApiKey ----

    @Test
    void shouldSaveAndFindApiKey() {
        UUID merchantId = UUID.randomUUID();

        ApiKey key = new ApiKey();
        key.setMerchantId(merchantId);
        key.setName("Test Key");
        key.setKeyHash("test_key_hash");
        key.setStatus(ApiKey.KeyStatus.ACTIVE);
        key.setMode(ApiKey.Mode.TEST);

        ApiKey saved = apiKeyRepo.save(key);
        assertNotNull(saved.getId());

        Optional<ApiKey> found = apiKeyRepo.findByKeyHash("test_key_hash");
        assertTrue(found.isPresent());
    }

    // ---- helpers ----

    private PaymentIntent createAndSaveIntent(UUID merchantId, String idempotencyKey) {
        PaymentIntent intent = new PaymentIntent();
        intent.setMerchantId(merchantId);
        intent.setAmount(BigInteger.valueOf(1000));
        intent.setCurrency("usd");
        intent.setStatus(PaymentIntent.PaymentStatus.SUCCEEDED);
        intent.setIdempotencyKey(idempotencyKey);
        intent.setMode(PaymentIntent.Mode.TEST);
        intent.setCaptureMethod(PaymentIntent.CaptureMethod.AUTOMATIC);
        return paymentIntentRepo.save(intent);
    }
}