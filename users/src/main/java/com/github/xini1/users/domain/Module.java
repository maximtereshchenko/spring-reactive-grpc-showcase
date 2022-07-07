package com.github.xini1.users.domain;

import com.github.xini1.common.event.*;
import com.github.xini1.users.port.*;
import com.github.xini1.users.usecase.*;

/**
 * @author Maxim Tereshchenko
 */
public final class Module {

    private final UserService userService;

    public Module(
            UserStore userStore,
            BasicEventStore basicEventStore,
            TokenProvider tokenProvider,
            HashingAlgorithm hashingAlgorithm
    ) {
        userService = new UserService(new Users(userStore, basicEventStore), tokenProvider, hashingAlgorithm);
    }

    public LoginUseCase loginUseCase() {
        return userService;
    }

    public RegisterUseCase registerUseCase() {
        return userService;
    }
}
