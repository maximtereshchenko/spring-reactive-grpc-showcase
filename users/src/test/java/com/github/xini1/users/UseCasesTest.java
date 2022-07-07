package com.github.xini1.users;

import com.github.xini1.common.*;
import com.github.xini1.common.event.user.*;
import com.github.xini1.users.domain.Module;
import com.github.xini1.users.exception.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Maxim Tereshchenko
 */
final class UseCasesTest {

    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module(
            new InMemoryUserStore(),
            eventStore,
            userId -> "jwt",
            password -> password
    );

    @Test
    void givenUserWithLoginAndPasswordDoNotExist_whenLogin_thenIncorrectLoginOrPasswordThrown() {
        var useCase = module.loginUseCase();

        assertThatThrownBy(() -> useCase.login("username", "password"))
                .isInstanceOf(IncorrectUsernameOrPassword.class);
    }

    @Test
    void givenUserWithLoginAndPasswordExists_whenLogin_thenJwtReturned() {
        var userId = module.registerUseCase().register("username", "password", UserType.REGULAR);

        assertThat(module.loginUseCase().login("username", "password")).isEqualTo("jwt");
        assertThat(eventStore.events()).containsExactly(new UserRegistered(1, userId, "username"));
    }

    @Test
    void givenPasswordIsEmpty_whenRegister_thenPasswordIsEmptyThrown() {
        var useCase = module.registerUseCase();

        assertThatThrownBy(() -> useCase.register("username", "", UserType.REGULAR))
                .isInstanceOf(PasswordIsEmpty.class);
    }

    @Test
    void givenUsernameIsEmpty_whenRegister_thenUsernameIsEmptyThrown() {
        var useCase = module.registerUseCase();

        assertThatThrownBy(() -> useCase.register("", "password", UserType.REGULAR))
                .isInstanceOf(UsernameIsEmpty.class);
    }

    @Test
    void givenUserWithSuchNameExists_whenRegister_thenUsernameIsTakenThrown() {
        var useCase = module.registerUseCase();
        useCase.register("username", "password1", UserType.REGULAR);

        assertThatThrownBy(() -> useCase.register("username", "password2", UserType.REGULAR))
                .isInstanceOf(UsernameIsTaken.class);
    }
}
