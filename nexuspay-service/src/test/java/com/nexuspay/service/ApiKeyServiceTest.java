package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.ApiKey;
import com.nexuspay.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApiKeyServiceTest {

    @Mock private ApiKeyRepository apiKeyRepository;
    @Mock private CryptoUtil cryptoUtil;

    @InjectMocks private ApiKeyService apiKeyService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnPlainKeyOnceWithoutPersistingPlainSecret() {
        UUID merchantId = UUID.randomUUID();
        when(cryptoUtil.generateToken()).thenReturn("abc1234567890");
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiKeyService.CreatedApiKey created = apiKeyService.create(
                merchantId, ApiKey.Mode.TEST, ApiKey.KeyType.SECRET, "main");

        assertEquals("sk_test_abc1234567890", created.key());
        assertEquals("sk_test_ab", created.prefix());

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        ApiKey stored = captor.getValue();
        assertEquals(merchantId, stored.getMerchantId());
        assertEquals("sk_test_ab", stored.getPrefix());
        assertFalse(created.key().equals(stored.getKeyHash()));
    }

    @Test
    void shouldListOnlyApiKeySummaries() {
        UUID merchantId = UUID.randomUUID();
        ApiKey key = new ApiKey();
        key.setId(UUID.randomUUID());
        key.setMerchantId(merchantId);
        key.setMode(ApiKey.Mode.TEST);
        key.setType(ApiKey.KeyType.SECRET);
        key.setPrefix("sk_test_ab");
        key.setName("main");
        key.setStatus(ApiKey.KeyStatus.ACTIVE);
        key.setKeyHash("hash");
        when(apiKeyRepository.findByMerchantId(merchantId)).thenReturn(List.of(key));

        List<ApiKeyService.ApiKeySummary> summaries = apiKeyService.listApiKeys(merchantId);

        assertEquals(1, summaries.size());
        assertEquals("sk_test_ab", summaries.get(0).prefix());
        assertTrue(List.of(ApiKeyService.ApiKeySummary.class.getRecordComponents()).stream()
                .noneMatch(component -> component.getName().contains("key")
                        || component.getName().contains("hash")));
    }

    @Test
    void shouldRevokeOnlyMerchantScopedKey() {
        UUID merchantId = UUID.randomUUID();
        UUID keyId = UUID.randomUUID();
        when(apiKeyRepository.findByMerchantIdAndId(merchantId, keyId)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                apiKeyService.revoke(merchantId, keyId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
