package com.github.xini1.users.domain;

import com.github.xini1.common.*;
import com.github.xini1.users.exception.*;
import com.github.xini1.users.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class User {

    private final UUID id;
    private final String name;
    private final String passwordHash;
    private final UserType userType;

    private User(UUID id, String name, String passwordHash, UserType userType) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.userType = userType;
    }

    public User(UserStore.Dto dto) {
        this(dto.getId(), dto.getUsername(), dto.getPasswordHash(), dto.getUserType());
    }

    static User create(String name, String password, UserType userType, HashingAlgorithm hashingAlgorithm) {
        if (name.isBlank()) {
            throw new UsernameIsEmpty();
        }
        if (password.isBlank()) {
            throw new PasswordIsEmpty();
        }
        return new User(UUID.randomUUID(), name, hashingAlgorithm.hash(password), userType);
    }

    UUID id() {
        return id;
    }

    String name() {
        return name;
    }

    UserStore.Dto dto() {
        return new UserStore.Dto(id, name, passwordHash, userType);
    }

    String jwt(TokenProvider tokenProvider) {
        return tokenProvider.sign(id);
    }
}
