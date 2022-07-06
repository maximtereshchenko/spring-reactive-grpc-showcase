package com.github.xini1;

import com.github.xini1.domain.Module;
import com.github.xini1.event.cart.*;
import com.github.xini1.event.item.*;
import com.github.xini1.exception.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
    void givenQuantityIsLessThan1_whenAddItemToCart_thenQuantityIsNotPositiveThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, User.REGULAR, itemId, -1))
                .isInstanceOf(QuantityIsNotPositive.class);

        assertThat(eventStore.events()).containsExactly(new ItemCreated(1, userId, itemId, "item"));
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
                        new ItemsOrdered(2, userId)
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

    @Test
    void givenUserIsAdmin_whenRemoveItemFromCart_thenUserIsNotRegularThrown() {
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, User.ADMIN, nonExistentItemId, 1))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenRemoveItemFromCart_thenItemIsNotFoundThrown() {
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, User.REGULAR, nonExistentItemId, 1))
                .isInstanceOf(ItemIsNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenQuantityIsLessThan1_whenRemoveItemFromCart_thenQuantityIsNotPositiveThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, User.REGULAR, itemId, -1))
                .isInstanceOf(QuantityIsNotPositive.class);

        assertThat(eventStore.events()).containsExactly(new ItemCreated(1, userId, itemId, "item"));
    }

    @Test
    void givenQuantityIsMoreThanCartHas_whenRemoveItemFromCart_thenQuantityIsMoreThanCartHasThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, User.REGULAR, itemId, 1);
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, User.REGULAR, itemId, 2))
                .isInstanceOf(QuantityIsMoreThanCartHas.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId, 1)
                );
    }

    @Test
    void givenQuantityIsLessOrEqualThanCartHas_whenRemoveItemFromCart_thenItemRemovedFromCartEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, User.REGULAR, itemId, 2);
        module.removeItemFromCartUseCase()
                .remove(userId, User.REGULAR, itemId, 1);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemAddedToCart(1, userId, itemId, 2),
                        new ItemRemovedFromCart(1, userId, itemId, 1)
                );
    }
}
