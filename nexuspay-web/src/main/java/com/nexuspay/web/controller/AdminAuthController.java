package com.nexuspay.web.controller;

import com.nexuspay.service.AdminAuthService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(adminAuthService.login(
                new AdminAuthService.LoginRequest(req.email(), req.password())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        return ResponseEntity.ok(adminAuthService.refresh(req.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestAttribute("userId") java.util.UUID userId) {
        adminAuthService.logout(userId);
        return ResponseEntity.ok().build();
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
}