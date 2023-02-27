package com.github.xini1.users.port;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface TokenProvider {

    String sign(UUID userId);

    UUID decode(String jwt);
}
