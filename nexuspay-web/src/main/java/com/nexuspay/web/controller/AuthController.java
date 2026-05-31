package com.nexuspay.web.controller;

import com.nexuspay.service.AuthService;
import com.nexuspay.service.PasswordResetService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(
                new AuthService.RegisterRequest(req.email(), req.password(), req.organizationName(), req.merchantName())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(new AuthService.LoginRequest(req.email(), req.password())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest req) {
        return ResponseEntity.ok(passwordResetService.requestReset(req.email()));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest req) {
        return ResponseEntity.ok(passwordResetService.confirmReset(req.token(), req.newPassword()));
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @Size(min = 8) @NotBlank String password,
            @NotBlank String organizationName,
            @NotBlank String merchantName) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record PasswordResetRequest(@Email @NotBlank String email) {}
    public record PasswordResetConfirmRequest(@NotBlank String token, @Size(min = 8) @NotBlank String newPassword) {}
}
