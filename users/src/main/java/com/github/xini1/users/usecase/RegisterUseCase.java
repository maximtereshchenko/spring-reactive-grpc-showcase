package com.github.xini1.users.usecase;

import com.github.xini1.common.UserType;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface RegisterUseCase {

    UUID register(String username, String password, UserType userType);
}
