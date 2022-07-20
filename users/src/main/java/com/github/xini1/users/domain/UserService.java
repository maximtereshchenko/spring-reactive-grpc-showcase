package com.github.xini1.users.domain;

import com.github.xini1.common.*;
import com.github.xini1.users.exception.*;
import com.github.xini1.users.port.*;
import com.github.xini1.users.usecase.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class UserService implements LoginUseCase, RegisterUseCase, DecodeJwtUseCase {

    private final Users users;
    private final TokenProvider tokenProvider;

    UserService(Users users, TokenProvider tokenProvider) {
        this.users = users;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public String login(String username, String password) {
        return users.findByUsernameAndPasswordHash(username, password)
                .map(user -> user.jwt(tokenProvider))
                .orElseThrow(IncorrectUsernameOrPassword::new);
    }

    @Override
    public UUID register(String username, String password, UserType userType) {
        var user = User.create(username, password, userType);
        users.save(user);
        return user.id();
    }

    @Override
    public Response decode(String jwt) {
        return users.find(tokenProvider.decode(jwt)).info();
    }
}
