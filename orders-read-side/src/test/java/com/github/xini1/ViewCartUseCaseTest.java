package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.ViewCartUseCase;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewCartUseCaseTest {

    private final Module module = new Module();
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenNoItemAddedToCartEvents_whenViewCart_thenCartIsEmpty() {
        assertThat(module.viewCartUseCase().view(userId, User.REGULAR)).isEqualTo(new ViewCartUseCase.CartView());
    }

    @Test
    void givenUserIsNotRegular_whenViewCart_thenUserIsNotRegularThrown() {
        var useCase = module.viewCartUseCase();

        assertThatThrownBy(() -> useCase.view(userId, User.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);
    }

    @Test
    void givenItemDoNotExist_whenOnItemAddedToCartEvent_thenItemIsNotFoundThrown() {
        var useCase = module.onItemAddedToCartEventUseCase();
        var itemAddedToCart = new ItemAddedToCart(1, userId, itemId, 1);

        assertThatThrownBy(() -> useCase.onEvent(itemAddedToCart))
                .isInstanceOf(ItemIsNotFound.class);
    }
}
