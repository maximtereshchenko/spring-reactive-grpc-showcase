package com.github.xini1.users;

import com.github.xini1.common.UserType;
import com.github.xini1.common.event.user.UserRegistered;
import com.github.xini1.users.domain.Module;
import com.github.xini1.users.exception.IncorrectUsernameOrPassword;
import com.github.xini1.users.exception.PasswordIsEmpty;
import com.github.xini1.users.exception.UsernameIsEmpty;
import com.github.xini1.users.exception.UsernameIsTaken;
import com.github.xini1.users.usecase.DecodeJwtUseCase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Maxim Tereshchenko
 */
final class UseCasesTest {

    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module(
            new InMemoryUserStore(),
            eventStore,
            new PlainStringTokenProvider(),
            new NoHashingAlgorithm()
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

        assertThat(module.loginUseCase().login("username", "password")).isEqualTo(userId.toString());
        assertThat(eventStore.events()).containsExactly(new UserRegistered(userId, "username", 1));
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

    @Test
    void givenJwt_whenDecodeJwt_thenUserIdReturned() {
        var userId = module.registerUseCase().register("username", "password", UserType.REGULAR);
        var jwt = module.loginUseCase().login("username", "password");

        assertThat(module.decodeJwtUseCase().decode(jwt))
                .isEqualTo(new DecodeJwtUseCase.Response(userId, UserType.REGULAR));
    }
}
