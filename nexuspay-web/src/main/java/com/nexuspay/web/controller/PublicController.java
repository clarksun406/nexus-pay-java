package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.PaymentIntent;
import com.nexuspay.service.PaymentLinkService;
import com.nexuspay.service.ThreeDsService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pub")
@RequiredArgsConstructor
public class PublicController {
    
    private final PaymentLinkService paymentLinkService;
    private final ThreeDsService threeDsService;
    
    @GetMapping("/pay/{token}")
    public ResponseEntity<?> getPaymentLink(@PathVariable String token) {
        var link = paymentLinkService.getByToken(token);
        return ResponseEntity.ok(Map.of(
                "title", link.getTitle(),
                "description", link.getDescription(),
                "amount", link.getAmount(),
                "currency", link.getCurrency(),
                "token", link.getToken()
        ));
    }
    
    @PostMapping("/pay/{token}")
    public ResponseEntity<?> submitPayment(
            @PathVariable String token,
            @RequestBody SubmitPaymentRequest req) {
        PaymentIntent intent = paymentLinkService.createPaymentFromLink(
                token, req.paymentMethodType(), req.paymentMethodId());
        
        return ResponseEntity.ok(Map.of(
                "id", intent.getId(),
                "status", intent.getStatus(),
                "amount", intent.getAmount(),
                "currency", intent.getCurrency()
        ));
    }
    
    @GetMapping("/3ds/challenge/{challengeId}")
    public ResponseEntity<?> getChallenge(@PathVariable String challengeId) {
        var challenge = threeDsService.getChallenge(challengeId);
        return ResponseEntity.ok(Map.of(
                "acsUrl", challenge.getAcsUrl(),
                "creq", challenge.getCreq(),
                "status", challenge.getStatus()
        ));
    }
    
    @PostMapping("/3ds/challenge/{challengeId}/complete")
    public ResponseEntity<?> completeChallenge(
            @PathVariable String challengeId,
            @RequestBody CompleteChallengeRequest req) {
        threeDsService.completeChallenge(challengeId, req.cres());
        return ResponseEntity.ok(Map.of("status", "completed"));
    }
    
    public record SubmitPaymentRequest(@NotBlank String paymentMethodType, 
                                       @NotBlank String paymentMethodId) {}
    
    public record CompleteChallengeRequest(String cres) {}
}
