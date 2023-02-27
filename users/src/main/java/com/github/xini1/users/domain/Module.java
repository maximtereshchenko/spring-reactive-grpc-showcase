package com.github.xini1.users.domain;

import com.github.xini1.common.event.BasicEventStore;
import com.github.xini1.users.port.HashingAlgorithm;
import com.github.xini1.users.port.TokenProvider;
import com.github.xini1.users.port.UserStore;
import com.github.xini1.users.usecase.DecodeJwtUseCase;
import com.github.xini1.users.usecase.LoginUseCase;
import com.github.xini1.users.usecase.RegisterUseCase;

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
        userService = new UserService(new Users(userStore, basicEventStore, hashingAlgorithm), tokenProvider);
    }

    public LoginUseCase loginUseCase() {
        return userService;
    }

    public RegisterUseCase registerUseCase() {
        return userService;
    }

    public DecodeJwtUseCase decodeJwtUseCase() {
        return userService;
    }
}
