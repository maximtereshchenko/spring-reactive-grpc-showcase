package com.github.xini1;

import com.github.xini1.domain.Module;
import com.github.xini1.exception.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Maxim Tereshchenko
 */
final class UseCasesTest {

    private final Module module = new Module();

    @Test
    void givenUserWithLoginAndPasswordDoNotExist_whenLogin_thenIncorrectLoginOrPasswordThrown() {
        var useCase = module.loginUseCase();

        assertThatThrownBy(() -> useCase.login("username", "password"))
                .isInstanceOf(IncorrectLoginOrPassword.class);
    }
}
