package com.nexuspay.web.controller;

import com.nexuspay.domain.entity.MerchantUser;
import com.nexuspay.domain.entity.User;
import com.nexuspay.repository.MerchantUserRepository;
import com.nexuspay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {
    
    private final UserRepository userRepository;
    private final MerchantUserRepository merchantUserRepository;
    
    @GetMapping
    public ResponseEntity<?> getCurrentUser(@RequestAttribute("userId") UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MerchantUser> memberships = merchantUserRepository.findByUserId(userId);
        
        List<Map<String, Object>> membershipList = memberships.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("merchantId", m.getMerchantId());
                    map.put("role", m.getRole());
                    map.put("status", m.getStatus());
                    return map;
                }).toList();
        
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "mfaEnabled", user.getMfaEnabled(),
                "memberships", membershipList
        ));
    }
}
