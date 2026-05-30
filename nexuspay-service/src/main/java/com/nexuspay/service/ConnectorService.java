package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectorService {
    
    private final ProviderAccountRepository providerAccountRepository;
    
    @Transactional
    public ProviderAccount create(UUID merchantId, CreateConnectorRequest req) {
        ProviderAccount account = new ProviderAccount();
        account.setMerchantId(merchantId);
        account.setProvider(req.provider());
        account.setMode(req.mode() != null ? req.mode() : ProviderAccount.Mode.TEST);
        account.setLabel(req.label());
        account.setEncryptedSecretKey(req.encryptedSecretKey());
        account.setEncryptedPublishableKey(req.encryptedPublishableKey());
        account.setWeight(req.weight() != null ? req.weight() : 1);
        account.setIsPrimary(req.isPrimary() != null ? req.isPrimary() : false);
        
        if (Boolean.TRUE.equals(account.getIsPrimary())) {
            providerAccountRepository.findByMerchantIdAndIsPrimaryTrue(merchantId)
                    .ifPresent(existing -> {
                        existing.setIsPrimary(false);
                        providerAccountRepository.save(existing);
                    });
        }
        
        return providerAccountRepository.save(account);
    }
    
    @Transactional
    public ProviderAccount update(UUID accountId, UpdateConnectorRequest req) {
        ProviderAccount account = providerAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Connector not found", HttpStatus.NOT_FOUND));
        
        if (req.label() != null) account.setLabel(req.label());
        if (req.weight() != null) account.setWeight(req.weight());
        if (req.status() != null) account.setStatus(req.status());
        
        if (Boolean.TRUE.equals(req.isPrimary())) {
            providerAccountRepository.findByMerchantIdAndIsPrimaryTrue(account.getMerchantId())
                    .filter(existing -> !existing.getId().equals(accountId))
                    .ifPresent(existing -> {
                        existing.setIsPrimary(false);
                        providerAccountRepository.save(existing);
                    });
            account.setIsPrimary(true);
        }
        
        return providerAccountRepository.save(account);
    }
    
    public List<ProviderAccount> listConnectors(UUID merchantId) {
        return providerAccountRepository.findByMerchantId(merchantId);
    }
    
    public ProviderAccount getConnector(UUID accountId) {
        return providerAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Connector not found", HttpStatus.NOT_FOUND));
    }
    
    @Transactional
    public void delete(UUID accountId) {
        providerAccountRepository.deleteById(accountId);
    }
    
    public record CreateConnectorRequest(ProviderAccount.Provider provider, ProviderAccount.Mode mode,
                                         String label, String encryptedSecretKey, String encryptedPublishableKey,
                                         Integer weight, Boolean isPrimary) {}
    
    public record UpdateConnectorRequest(String label, Integer weight, Boolean isPrimary,
                                         ProviderAccount.ConnectorStatus status) {}
}
