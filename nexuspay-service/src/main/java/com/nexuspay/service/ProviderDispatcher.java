package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.ProviderAccount;
import com.nexuspay.repository.ProviderAccountRepository;
import com.nexuspay.service.provider.PaymentProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderDispatcher {

    private final ProviderAccountRepository providerAccountRepository;
    private final List<PaymentProvider> paymentProviders;

    private final Map<ProviderAccount.Provider, PaymentProvider> providers = new EnumMap<>(ProviderAccount.Provider.class);

    @PostConstruct
    public void init() {
        providers.clear();
        for (PaymentProvider provider : paymentProviders) {
            providers.put(provider.supportedProvider(), provider);
        }
    }

    public PaymentIntentService.ChargeResult charge(ProviderAccount.Provider provider, PaymentIntent intent, String paymentMethodId) {
        ProviderAccount account = providerAccountRepository.findById(intent.getConnectorAccountId())
                .orElseThrow(() -> new BusinessException("Provider account not found", HttpStatus.NOT_FOUND));

        PaymentProvider paymentProvider = providers.get(provider);
        if (paymentProvider == null) {
            throw new BusinessException("Unsupported provider: " + provider, HttpStatus.BAD_REQUEST);
        }

        return paymentProvider.charge(intent, paymentMethodId, account);
    }

    public boolean capture(ProviderAccount.Provider provider, String providerPaymentId, UUID accountId) {
        ProviderAccount account = providerAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Provider account not found", HttpStatus.NOT_FOUND));

        PaymentProvider paymentProvider = providers.get(provider);
        return paymentProvider != null && paymentProvider.capture(providerPaymentId, account);
    }

    public boolean cancel(ProviderAccount.Provider provider, String providerPaymentId, UUID accountId) {
        ProviderAccount account = providerAccountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Provider account not found", HttpStatus.NOT_FOUND));

        PaymentProvider paymentProvider = providers.get(provider);
        return paymentProvider != null && paymentProvider.cancel(providerPaymentId, account);
    }
}