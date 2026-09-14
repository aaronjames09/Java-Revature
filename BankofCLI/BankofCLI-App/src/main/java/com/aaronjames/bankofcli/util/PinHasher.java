package com.aaronjames.bankofcli.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 hashing for account PINs.
 *
 * Never store a raw PIN. A per-account random salt means two users with the
 * same PIN don't produce the same hash, and precomputed "rainbow table"
 * attacks stop working.
 *
 * Note: for a real production system you'd reach for a slow, purpose-built
 * algorithm like BCrypt/Argon2. SHA-256 + salt is a deliberate, honest
 * trade-off here to keep this learning project dependency-free - worth
 * calling out if this comes up in an interview.
 */
public final class PinHasher {

    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PinHasher() {
    }

    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String pin, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.getDecoder().decode(salt));
            byte[] hashed = digest.digest(pin.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to exist on every JVM - this is unreachable in practice.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean matches(String rawPin, String salt, String expectedHash) {
        return hash(rawPin, salt).equals(expectedHash);
    }
}
