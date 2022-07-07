package com.github.xini1.users;

import com.github.xini1.users.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class PlainStringTokenProvider implements TokenProvider {

    @Override
    public String sign(UUID userId) {
        return userId.toString();
    }

    @Override
    public UUID decode(String jwt) {
        return UUID.fromString(jwt);
    }
}
