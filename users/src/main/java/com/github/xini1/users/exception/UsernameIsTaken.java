package com.github.xini1.users.exception;

/**
 * @author Maxim Tereshchenko
 */
public final class UsernameIsTaken extends RuntimeException {

    public UsernameIsTaken(String username, Throwable cause) {
        super("Username " + username + " is taken", cause);
    }

    public UsernameIsTaken(String username) {
        this(username, null);
    }
}
