package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemsOrdered;
import com.github.xini1.event.item.ItemCreated;
import com.github.xini1.event.item.ItemDeactivated;
import com.github.xini1.exception.CartHasDeactivatedItem;
import com.github.xini1.exception.CartIsEmpty;
import com.github.xini1.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.User;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class RegularUserUseCasesTest {

    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module(eventStore);
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID nonExistentItemId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenUserIsAdmin_whenAddItemToCart_thenUserIsNotRegularThrown() {
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, User.ADMIN, nonExistentItemId, 1))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenAddItemToCart_thenItemIsNotFoundThrown() {
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, User.REGULAR, nonExistentItemId, 1))
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

        assertThatThrownBy(() -> useCase.add(userId, User.REGULAR, itemId, 1))
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
                .add(userId, User.REGULAR, itemId, 1);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId, 1)
                );
    }

    @Test
    void givenUserIsAdmin_whenOrderItemsInCart_thenUserIsNotRegularThrown() {
        var useCase = module.orderItemsInCartUseCase();

        assertThatThrownBy(() -> useCase.order(userId, User.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
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
                .add(userId, User.REGULAR, itemId, 1);

        module.orderItemsInCartUseCase()
                .order(userId, User.REGULAR);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId, 1),
                        new ItemsOrdered(2, userId, Map.of(itemId, 1))
                );
    }

    @Test
    void givenCartHasDeactivatedItem_whenOrderItemsInCart_thenCartHasDeactivatedItemThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, User.REGULAR, itemId, 1);
        module.deactivateItemUseCase()
                .deactivate(userId, User.ADMIN, itemId);
        var useCase = module.orderItemsInCartUseCase();

        assertThatThrownBy(() -> useCase.order(userId, User.REGULAR))
                .isInstanceOf(CartHasDeactivatedItem.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId, 1),
                        new ItemDeactivated(2, userId, itemId)
                );
    }
}
