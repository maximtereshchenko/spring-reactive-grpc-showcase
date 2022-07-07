package com.github.xini1.users.domain;

import com.github.xini1.users.port.*;
import com.github.xini1.users.usecase.*;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final UserService userService;

    public Module(
            UserStore userStore,
            EventStore eventStore,
            TokenProvider tokenProvider,
            HashingAlgorithm hashingAlgorithm
    ) {
        userService = new UserService(new Users(userStore, eventStore), tokenProvider, hashingAlgorithm);
    }

    public LoginUseCase loginUseCase() {
        return userService;
    }

    public RegisterUseCase registerUseCase() {
        return userService;
    }
}
