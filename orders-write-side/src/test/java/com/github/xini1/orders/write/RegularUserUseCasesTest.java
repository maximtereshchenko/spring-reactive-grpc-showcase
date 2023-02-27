package com.github.xini1.orders.write;

import com.github.xini1.common.UserType;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.orders.write.domain.Module;
import com.github.xini1.orders.write.exception.*;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Maxim Tereshchenko
 */
final class RegularUserUseCasesTest {

    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module(eventStore);
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID nonExistentItemId = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void givenUserIsAdmin_whenAddItemToCart_thenUserIsNotRegularThrown() {
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, UserType.ADMIN, nonExistentItemId, 1))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenAddItemToCart_thenItemIsNotFoundThrown() {
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, UserType.REGULAR, nonExistentItemId, 1))
                .isInstanceOf(ItemIsNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenQuantityIsLessThan1_whenAddItemToCart_thenQuantityIsNotPositiveThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, UserType.REGULAR, itemId, -1))
                .isInstanceOf(QuantityIsNotPositive.class);

        assertThat(eventStore.events()).containsExactly(new ItemCreated(itemId, userId, "item", 1));
    }

    @Test
    void givenDeactivatedItem_whenAddItemToCart_thenCouldNotAddDeactivatedItemToCartThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        module.deactivateItemUseCase()
                .deactivate(userId, UserType.ADMIN, itemId);
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, UserType.REGULAR, itemId, 1))
                .isInstanceOf(CouldNotAddDeactivatedItemToCart.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(itemId, userId, "item", 1),
                        new ItemDeactivated(itemId, userId, 2)
                );
    }

    @Test
    void givenActiveItem_whenAddItemToCart_thenItemAddedToCartEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");

        module.addItemToCartUseCase()
                .add(userId, UserType.REGULAR, itemId, 1);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(itemId, userId, "item", 1),
                        new ItemAddedToCart(userId, itemId, 1, 1)
                );
    }

    @Test
    void givenUserIsAdmin_whenOrderItemsInCart_thenUserIsNotRegularThrown() {
        var useCase = module.orderItemsInCartUseCase();

        assertThatThrownBy(() -> useCase.order(userId, UserType.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenCartIsEmpty_whenOrderItemsInCart_thenCartIsEmptyThrown() {
        var useCase = module.orderItemsInCartUseCase();

        assertThatThrownBy(() -> useCase.order(userId, UserType.REGULAR))
                .isInstanceOf(CartIsEmpty.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenCartIsNotEmpty_whenOrderItemsInCart_thenItemsOrderedEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, UserType.REGULAR, itemId, 1);

        module.orderItemsInCartUseCase()
                .order(userId, UserType.REGULAR);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(itemId, userId, "item", 1),
                        new ItemAddedToCart(userId, itemId, 1, 1),
                        new ItemsOrdered(userId, 2)
                );
    }

    @Test
    void givenCartHasDeactivatedItem_whenOrderItemsInCart_thenCartHasDeactivatedItemThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, UserType.REGULAR, itemId, 1);
        module.deactivateItemUseCase()
                .deactivate(userId, UserType.ADMIN, itemId);
        var useCase = module.orderItemsInCartUseCase();

        assertThatThrownBy(() -> useCase.order(userId, UserType.REGULAR))
                .isInstanceOf(CartHasDeactivatedItem.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(itemId, userId, "item", 1),
                        new ItemAddedToCart(userId, itemId, 1, 1),
                        new ItemDeactivated(itemId, userId, 2)
                );
    }

    @Test
    void givenUserIsAdmin_whenRemoveItemFromCart_thenUserIsNotRegularThrown() {
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, UserType.ADMIN, nonExistentItemId, 1))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenRemoveItemFromCart_thenItemIsNotFoundThrown() {
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, UserType.REGULAR, nonExistentItemId, 1))
                .isInstanceOf(ItemIsNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenQuantityIsLessThan1_whenRemoveItemFromCart_thenQuantityIsNotPositiveThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, UserType.REGULAR, itemId, -1))
                .isInstanceOf(QuantityIsNotPositive.class);

        assertThat(eventStore.events()).containsExactly(new ItemCreated(itemId, userId, "item", 1));
    }

    @Test
    void givenQuantityIsMoreThanCartHas_whenRemoveItemFromCart_thenQuantityIsMoreThanCartHasThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, UserType.REGULAR, itemId, 1);
        var useCase = module.removeItemFromCartUseCase();

        assertThatThrownBy(() -> useCase.remove(userId, UserType.REGULAR, itemId, 2))
                .isInstanceOf(QuantityIsMoreThanCartHas.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(itemId, userId, "item", 1),
                        new ItemAddedToCart(userId, itemId, 1, 1)
                );
    }

    @Test
    void givenQuantityIsLessOrEqualThanCartHas_whenRemoveItemFromCart_thenItemRemovedFromCartEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, UserType.ADMIN, "item");
        module.addItemToCartUseCase()
                .add(userId, UserType.REGULAR, itemId, 2);
        module.removeItemFromCartUseCase()
                .remove(userId, UserType.REGULAR, itemId, 1);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(itemId, userId, "item", 1),
                        new ItemAddedToCart(userId, itemId, 2, 1),
                        new ItemRemovedFromCart(userId, itemId, 1, 2)
                );
    }
}
