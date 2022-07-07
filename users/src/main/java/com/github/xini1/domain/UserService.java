package com.github.xini1.domain;

import com.github.xini1.exception.*;
import com.github.xini1.usecase.*;

/**
 * @author Maxim Tereshchenko
 */
final class UserService implements LoginUseCase {

    @Override
    public String login(String username, String password) {
        throw new IncorrectLoginOrPassword();
    }
}
