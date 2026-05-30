package com.nexuspay.web.controller;

import com.nexuspay.service.MfaService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
public class MfaController {
    
    private final MfaService mfaService;
    
    @PostMapping("/setup")
    public ResponseEntity<?> setup(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(mfaService.setupMfa(userId));
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @RequestAttribute("userId") UUID userId,
            @RequestBody ConfirmMfaRequest req) {
        return ResponseEntity.ok(mfaService.confirmMfa(userId, req.code()));
    }
    
    @PostMapping("/disable")
    public ResponseEntity<?> disable(
            @RequestAttribute("userId") UUID userId,
            @RequestBody DisableMfaRequest req) {
        mfaService.disableMfa(userId, req.code());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestAttribute("userId") UUID userId,
            @RequestBody VerifyMfaRequest req) {
        return ResponseEntity.ok(mfaService.verifyMfa(userId, req.code()));
    }
    
    public record ConfirmMfaRequest(@NotBlank String code) {}
    public record DisableMfaRequest(@NotBlank String code) {}
    public record VerifyMfaRequest(@NotBlank String code) {}
}
