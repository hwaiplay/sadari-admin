package org.sadari.admin.sadariadmin.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 비밀번호 해시 유틸
 */
public final class PasswordHash {

    /**
     * 비밀번호 해시 유틸 생성 방지
     * @Author SeungHyeon.Kang
     * @return
     */
    private PasswordHash() {
    }

    /**
     * SHA256 해시 생성
     * @Author SeungHyeon.Kang
     * @param value
     * @return
     */
    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
