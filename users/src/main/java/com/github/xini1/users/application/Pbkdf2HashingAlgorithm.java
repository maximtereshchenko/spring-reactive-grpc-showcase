package com.github.xini1.users.application;

import com.github.xini1.users.port.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

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
