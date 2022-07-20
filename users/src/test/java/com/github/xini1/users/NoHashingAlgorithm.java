package com.github.xini1.users;

import com.github.xini1.users.port.*;

/**
 * @author Maxim Tereshchenko
 */
final class NoHashingAlgorithm implements HashingAlgorithm {

    @Override
    public String hash(String password, byte[] salt) {
        return password;
    }

    @Override
    public byte[] salt() {
        return new byte[0];
    }
}
