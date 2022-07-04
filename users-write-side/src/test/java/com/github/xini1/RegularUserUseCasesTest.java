package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.event.ItemAddedToCart;
import com.github.xini1.event.ItemCreated;
import com.github.xini1.event.ItemDeactivated;
import com.github.xini1.event.ItemsOrdered;
import com.github.xini1.exception.CartIsEmpty;
import com.github.xini1.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class RegularUserUseCasesTest {

    private final IncrementedIdentifiers identifiers = new IncrementedIdentifiers();
    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module.Builder()
            .with(eventStore)
            .with(identifiers)
            .build();
    private final UUID userId = identifiers.uuid(1);

    @Test
    void givenUserIsAdmin_whenAddItemToCart_thenUserIsNotRegularThrown() {
        var useCase = module.addItemToCartUseCase();
        var itemId = identifiers.uuid(1);

        assertThatThrownBy(() -> useCase.add(userId, User.ADMIN, itemId))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenAddItemToCart_thenItemIsNotFoundThrown() {
        var useCase = module.addItemToCartUseCase();
        var itemId = identifiers.uuid(1);

        assertThatThrownBy(() -> useCase.add(userId, User.REGULAR, itemId))
                .isInstanceOf(ItemIsNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenDeactivatedItem_whenAddItemToCart_thenCouldNotAddDeactivatedItemToCartThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        module.deactivateItemUseCase()
                .deactivate(userId, User.ADMIN, itemId);
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, User.REGULAR, itemId))
                .isInstanceOf(CouldNotAddDeactivatedItemToCart.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemDeactivated(2, userId, itemId)
                );
    }

    @Test
    void givenActiveItem_whenAddItemToCart_thenItemAddedToCartEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");

        module.addItemToCartUseCase()
                .add(userId, User.REGULAR, itemId);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId)
                );
    }

    @Test
    void givenCartIsEmpty_whenOrderItemsInCart_thenCartIsEmptyThrown() {
        var useCase = module.orderItemsInCartUseCase();

        assertThatThrownBy(() -> useCase.order(userId, User.REGULAR))
                .isInstanceOf(CartIsEmpty.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenCartIsNotEmpty_whenOrderItemsInCart_thenItemsOrderedEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, User.REGULAR, itemId);

        module.orderItemsInCartUseCase()
                .order(userId, User.REGULAR);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId),
                        new ItemsOrdered(2, userId)
                );
    }
}
