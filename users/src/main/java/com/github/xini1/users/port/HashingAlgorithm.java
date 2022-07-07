package com.github.xini1.users.port;

/**
 * @author Maxim Tereshchenko
 */
public interface HashingAlgorithm {

    String hash(String password);
}
