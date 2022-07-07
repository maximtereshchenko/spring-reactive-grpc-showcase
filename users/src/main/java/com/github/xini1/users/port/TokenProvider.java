package com.github.xini1.users.port;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface TokenProvider {

    String sign(UUID userId);
}
