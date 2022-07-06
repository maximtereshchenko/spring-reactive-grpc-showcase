package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemRemovedFromCart;
import com.github.xini1.exception.ItemIsNotFound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class OnEventUseCasesTest {

    private final Module module = new Module(new InMemoryViewStore());
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenItemDoNotExist_whenOnItemAddedToCartEvent_thenItemIsNotFoundThrown() {
        var useCase = module.onItemAddedToCartEventUseCase();
        var itemAddedToCart = new ItemAddedToCart(1, userId, itemId, 1);

        assertThatThrownBy(() -> useCase.onEvent(itemAddedToCart))
                .isInstanceOf(ItemIsNotFound.class);
    }

    @Test
    void givenItemDoNotExist_whenOnItemRemovedFromCartEvent_thenItemIsNotFoundThrown() {
        var useCase = module.onItemRemovedFromCartEventUseCase();
        var itemAddedToCart = new ItemRemovedFromCart(1, userId, itemId, 1);

        assertThatThrownBy(() -> useCase.onEvent(itemAddedToCart))
                .isInstanceOf(ItemIsNotFound.class);
    }
}
