package com.nexuspay.service;

import com.nexuspay.common.exception.BusinessException;
import com.nexuspay.common.util.CryptoUtil;
import com.nexuspay.domain.entity.User;
import com.nexuspay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final UserRepository userRepository;
    private final CryptoUtil cryptoUtil;
    private final Base32 base32 = new Base32();

    @Transactional
    public MfaSetupResponse setupMfa(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        String secret = generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        user.setMfaBackupCodes(null);
        userRepository.save(user);

        String otpAuthUrl = String.format(
                "otpauth://totp/NexusPay:%s?secret=%s&issuer=NexusPay",
                user.getEmail(), secret);

        return new MfaSetupResponse(secret, otpAuthUrl);
    }

    @Transactional
    public boolean confirmMfa(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (user.getMfaSecret() == null) {
            throw new BusinessException("MFA not set up", HttpStatus.BAD_REQUEST);
        }

        if (!verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Invalid code", HttpStatus.BAD_REQUEST);
        }

        user.setMfaEnabled(true);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public void disableMfa(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (!user.getMfaEnabled()) {
            throw new BusinessException("MFA not enabled", HttpStatus.BAD_REQUEST);
        }

        if (!verifyCode(user.getMfaSecret(), code)) {
            throw new BusinessException("Invalid code", HttpStatus.BAD_REQUEST);
        }

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaBackupCodes(null);
        userRepository.save(user);
    }

    @Transactional
    public boolean verifyMfa(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (!user.getMfaEnabled() || user.getMfaSecret() == null) {
            return true;
        }

        if (verifyCode(user.getMfaSecret(), code)) {
            return true;
        }

        return consumeBackupCode(user, code);
    }

    @Transactional
    public BackupCodesResponse regenerateBackupCodes(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getMfaEnabled()) || user.getMfaSecret() == null) {
            throw new BusinessException("MFA must be enabled before generating backup codes", HttpStatus.BAD_REQUEST);
        }

        List<String> rawCodes = generateRawBackupCodes(8);
        List<String> hashedCodes = rawCodes.stream()
                .map(this::hashBackupCode)
                .collect(Collectors.toList());

        user.setMfaBackupCodes(String.join(",", hashedCodes));
        userRepository.save(user);

        return new BackupCodesResponse(rawCodes, rawCodes.size());
    }

    public BackupCodesRemainingResponse getRemainingBackupCodes(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        return new BackupCodesRemainingResponse(parseBackupCodeHashes(user.getMfaBackupCodes()).size());
    }

    private boolean consumeBackupCode(User user, String code) {
        List<String> hashes = parseBackupCodeHashes(user.getMfaBackupCodes());
        if (hashes.isEmpty()) return false;

        String targetHash = hashBackupCode(code);
        int idx = hashes.indexOf(targetHash);
        if (idx < 0) return false;

        hashes.remove(idx);
        user.setMfaBackupCodes(hashes.isEmpty() ? null : String.join(",", hashes));
        userRepository.save(user);
        return true;
    }

    private List<String> parseBackupCodeHashes(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String hashBackupCode(String code) {
        return cryptoUtil.hashSha256(normalizeBackupCode(code), "mfa_backup");
    }

    private String normalizeBackupCode(String code) {
        return code == null ? "" : code.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private List<String> generateRawBackupCodes(int count) {
        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String raw = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            result.add(raw.substring(0, 5) + "-" + raw.substring(5));
        }
        return result;
    }

    private boolean verifyCode(String secret, String code) {
        try {
            long timeIndex = System.currentTimeMillis() / 1000 / 30;
            byte[] key = base32.decode(secret);

            for (int i = -1; i <= 1; i++) {
                byte[] expected = generateCode(key, timeIndex + i);
                byte[] actual = hexStringToByteArray(String.format("%016x", Long.parseLong(code)));

                if (Arrays.equals(expected, actual)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] generateCode(byte[] key, long timeIndex) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));

        byte[] time = new byte[8];
        for (int i = 7; i >= 0; i--) {
            time[i] = (byte) timeIndex;
            timeIndex >>= 8;
        }

        byte[] hash = mac.doFinal(time);
        int offset = hash[hash.length - 1] & 0xf;

        byte[] code = new byte[4];
        for (int i = 0; i < 4; i++) {
            code[i] = hash[offset + i];
        }

        code[0] &= 0x7f;
        return Arrays.copyOf(code, 8);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    public record MfaSetupResponse(String secret, String otpAuthUrl) {}
    public record BackupCodesResponse(List<String> backupCodes, int remaining) {}
    public record BackupCodesRemainingResponse(int remaining) {}
}
