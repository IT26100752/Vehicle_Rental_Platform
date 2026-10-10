package com.vehiclerental.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * INFORMATION HIDING / SECURITY
 * -----------------------------
 * Plain-text passwords are never stored and never compared.
 * We store a SHA-256 hash of the password instead, so even if someone opens
 * users.json (or the users table) they cannot read anybody's password.
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // utility class - no instances
    }

    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Constant-time-ish comparison to avoid timing attacks (nice viva point). */
    public static boolean matches(String plainPassword, String storedHash) {
        return MessageDigest.isEqual(
                hash(plainPassword).getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}
