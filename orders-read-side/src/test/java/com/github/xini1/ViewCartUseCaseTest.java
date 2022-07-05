package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.xini1.domain.Module;
import com.github.xini1.usecase.ViewCartUseCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewCartUseCaseTest {

    private final Module module = new Module();
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");

    @Test
    void givenNoAddItemToCartEvents_whenViewCart_thenCartIsEmpty() {
        assertThat(module.viewCartUseCase().view(userId, User.REGULAR)).isEqualTo(new ViewCartUseCase.CartView());
    }
}
