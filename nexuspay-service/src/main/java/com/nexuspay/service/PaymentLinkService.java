package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.domain.entity.PaymentLink;
import com.nexuspay.repository.PaymentLinkRepository;
import com.nexuspay.repository.ProviderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentLinkService {
    
    private final PaymentLinkRepository paymentLinkRepository;
    private final PaymentIntentService paymentIntentService;
    private final ProviderAccountRepository providerAccountRepository;
    private final CryptoUtil cryptoUtil;
    
    @Transactional
    public PaymentLink create(UUID merchantId, CreateRequest req) {
        PaymentLink link = new PaymentLink();
        link.setMerchantId(merchantId);
        link.setToken(cryptoUtil.generateToken().substring(0, 32));
        link.setTitle(req.title());
        link.setDescription(req.description());
        link.setAmount(req.amount());
        link.setCurrency(req.currency() != null ? req.currency() : "usd");
        link.setMode(req.mode() != null ? req.mode() : PaymentIntent.Mode.TEST);
        link.setRedirectUrl(req.redirectUrl());
        link.setPinnedConnectorId(req.pinnedConnectorId());
        
        return paymentLinkRepository.save(link);
    }
    
    @Transactional
    public PaymentLink update(UUID merchantId, UUID linkId, UpdateRequest req) {
        PaymentLink link = paymentLinkRepository.findByMerchantIdAndId(merchantId, linkId)
                .orElseThrow(() -> new BusinessException("Payment link not found", HttpStatus.NOT_FOUND));
        
        if (req.title() != null) link.setTitle(req.title());
        if (req.description() != null) link.setDescription(req.description());
        if (req.amount() != null) link.setAmount(req.amount());
        if (req.status() != null) link.setStatus(req.status());
        
        return paymentLinkRepository.save(link);
    }
    
    @Transactional
    public PaymentLink deactivate(UUID merchantId, UUID linkId) {
        PaymentLink link = paymentLinkRepository.findByMerchantIdAndId(merchantId, linkId)
                .orElseThrow(() -> new BusinessException("Payment link not found", HttpStatus.NOT_FOUND));
        link.setStatus(PaymentLink.LinkStatus.INACTIVE);
        return paymentLinkRepository.save(link);
    }
    
    public PaymentLink getByToken(String token) {
        PaymentLink link = paymentLinkRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Payment link not found", HttpStatus.NOT_FOUND));
        
        if (link.getStatus() != PaymentLink.LinkStatus.ACTIVE) {
            throw new BusinessException("Payment link is inactive", HttpStatus.BAD_REQUEST);
        }
        
        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Payment link expired", HttpStatus.BAD_REQUEST);
        }
        
        return link;
    }
    
    public PaymentLink get(UUID merchantId, UUID linkId) {
        return paymentLinkRepository.findByMerchantIdAndId(merchantId, linkId)
                .orElseThrow(() -> new BusinessException("Payment link not found", HttpStatus.NOT_FOUND));
    }
    
    public List<PaymentLink> list(UUID merchantId) {
        return paymentLinkRepository.findByMerchantId(merchantId);
    }
    
    @Transactional
    public PaymentIntent createPaymentFromLink(String token, String paymentMethodType, String paymentMethodId) {
        PaymentLink link = getByToken(token);
        
        PaymentIntent intent = paymentIntentService.create(link.getMerchantId(),
                new PaymentIntentService.CreateRequest(
                        link.getAmount(), link.getCurrency(), link.getMode(),
                        PaymentIntent.CaptureMethod.AUTOMATIC, null, null, null,
                        link.getTitle(), null, null));
        
        return paymentIntentService.confirm(link.getMerchantId(), intent.getId(),
                new PaymentIntentService.ConfirmRequest(paymentMethodType, paymentMethodId));
    }
    
    public record CreateRequest(String title, String description, BigInteger amount,
                               String currency, PaymentIntent.Mode mode,
                               String redirectUrl, UUID pinnedConnectorId) {}
    
    public record UpdateRequest(String title, String description, BigInteger amount,
                               PaymentLink.LinkStatus status) {}
}
