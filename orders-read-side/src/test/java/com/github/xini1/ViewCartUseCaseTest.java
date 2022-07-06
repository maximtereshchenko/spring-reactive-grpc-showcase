package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemRemovedFromCart;
import com.github.xini1.event.cart.ItemsOrdered;
import com.github.xini1.event.item.ItemActivated;
import com.github.xini1.event.item.ItemCreated;
import com.github.xini1.event.item.ItemDeactivated;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.view.Cart;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewCartUseCaseTest {

    private final Module module = new Module(new InMemoryViewStore());
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenUserIsNotRegular_whenViewCart_thenUserIsNotRegularThrown() {
        var useCase = module.viewCartUseCase();

        assertThatThrownBy(() -> useCase.view(userId, User.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);
    }

    @Test
    void givenNoItemAddedToCartEvents_whenViewCart_thenCartIsEmpty() {
        assertThat(module.viewCartUseCase().view(userId, User.REGULAR)).isEqualTo(new Cart(userId));
    }

    @Test
    void givenItemAddedToCartEvent_whenViewCart_thenCartHasThatItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                1,
                                new Cart.ItemInCart(itemId, "item", true, 1)
                        )
                );
    }

    @Test
    void givenItemDeactivatedEvent_whenViewCart_thenItemInCartIsDeactivated() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemDeactivatedEventUseCase().onEvent(new ItemDeactivated(2, userId, itemId));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                1,
                                new Cart.ItemInCart(itemId, "item", false, 1)
                        )
                );
    }

    @Test
    void givenItemActivatedEvent_whenViewCart_thenItemInCartIsActivated() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemDeactivatedEventUseCase().onEvent(new ItemDeactivated(2, userId, itemId));
        module.onItemActivatedEventUseCase().onEvent(new ItemActivated(3, userId, itemId));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                1,
                                new Cart.ItemInCart(itemId, "item", true, 1)
                        )
                );
    }

    @Test
    void givenCartHasItem_whenViewCart_thenCartHasMoreOfThatItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(2, userId, itemId, 2));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                2,
                                new Cart.ItemInCart(itemId, "item", true, 3)
                        )
                );
    }

    @Test
    void givenItemRemovedFromCartEvent_whenViewCart_thenCartHasNotThatItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemRemovedFromCartEventUseCase().onEvent(new ItemRemovedFromCart(2, userId, itemId, 1));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR)).isEqualTo(new Cart(userId, 2));
    }

    @Test
    void givenCartHasItem_whenViewCart_thenCartHasLessOfThatItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 2));
        module.onItemRemovedFromCartEventUseCase().onEvent(new ItemRemovedFromCart(2, userId, itemId, 1));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                2,
                                new Cart.ItemInCart(itemId, "item", true, 1)
                        )
                );
    }

    @Test
    void givenItemOrderedEvent_whenViewCart_thenCartIsEmpty() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemsOrderedEventUseCase().onEvent(new ItemsOrdered(2, userId));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR)).isEqualTo(new Cart(userId, 2));
    }

    @Test
    void givenCartVersionLessOrEqualToItemAddedToCartEventVersion_whenViewCart_thenCartWasNotChanged() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                1,
                                new Cart.ItemInCart(itemId, "item", true, 1)
                        )
                );
    }

    @Test
    void givenCartVersionLessOrEqualToItemRemovedFromCartEventVersion_whenViewCart_thenCartWasNotChanged() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemRemovedFromCartEventUseCase().onEvent(new ItemRemovedFromCart(1, userId, itemId, 1));

        assertThat(module.viewCartUseCase().view(userId, User.REGULAR))
                .isEqualTo(
                        new Cart(
                                userId,
                                1,
                                new Cart.ItemInCart(itemId, "item", true, 1)
                        )
                );
    }
}
