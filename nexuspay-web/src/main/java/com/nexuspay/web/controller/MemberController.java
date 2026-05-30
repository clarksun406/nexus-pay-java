package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.service.MemberService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    
    @GetMapping
    public ResponseEntity<?> list(@RequestAttribute("merchantId") UUID merchantId) {
        return ResponseEntity.ok(memberService.listMembers(merchantId));
    }
    
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateRole(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID userId,
            @RequestBody @NotNull MerchantUser.Role role) {
        return ResponseEntity.ok(memberService.updateRole(merchantId, userId, role));
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> remove(
            @RequestAttribute("merchantId") UUID merchantId,
            @PathVariable UUID userId) {
        memberService.removeMember(merchantId, userId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/invite")
    public ResponseEntity<?> invite(
            @RequestAttribute("merchantId") UUID merchantId,
            @RequestBody InviteRequest req) {
        return ResponseEntity.ok(memberService.inviteMember(
                merchantId, req.email(), req.role(), req.invitedBy()));
    }
    
    public record InviteRequest(@Email @NotBlank String email, 
                               @NotNull MerchantUser.Role role,
                               UUID invitedBy) {}
}
