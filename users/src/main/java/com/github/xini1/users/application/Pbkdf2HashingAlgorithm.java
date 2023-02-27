package com.github.xini1.users.application;

import com.github.xini1.users.port.HashingAlgorithm;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Random;

/**
 * @author Maxim Tereshchenko
 */
final class Pbkdf2HashingAlgorithm implements HashingAlgorithm {

    private final Random random = new SecureRandom();

    @Override
    public String hash(String password, byte[] salt) {
        try {
            return Base64.getEncoder()
                    .encodeToString(
                            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                                    .generateSecret(
                                            new PBEKeySpec(
                                                    password.toCharArray(),
                                                    salt,
                                                    65536,
                                                    128
                                            )
                                    )
                                    .getEncoded()
                    );
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public byte[] salt() {
        var salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
}
