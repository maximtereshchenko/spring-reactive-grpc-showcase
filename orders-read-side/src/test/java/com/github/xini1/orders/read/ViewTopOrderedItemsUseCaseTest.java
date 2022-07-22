package com.github.xini1.orders.read;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.common.UserType;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.orders.read.domain.Module;
import com.github.xini1.orders.read.exception.UserIsNotAdmin;
import com.github.xini1.orders.read.view.TopOrderedItem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewTopOrderedItemsUseCaseTest {

    private final Module module = new Module(new InMemoryViewStore());
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void givenUserIsNotAdmin_whenViewTopOrderedItems_thenUserIsNotAdminThrown() {
        var useCase = module.viewTopOrderedItemsUseCase();

        assertThatThrownBy(() -> useCase.view(UserType.REGULAR)).isInstanceOf(UserIsNotAdmin.class);
    }

    @Test
    void givenNoItemsOrdered_whenViewTopOrderedItems_thenIterableIsEmpty() {
        assertThat(module.viewTopOrderedItemsUseCase().view(UserType.ADMIN)).isEmpty();
    }

    @Test
    void givenItemCreatedEvent_whenViewTopOrderedItems_thenThatItemHasZeroTimesOrdered() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(itemId, userId, "item", 1));

        assertThat(module.viewTopOrderedItemsUseCase().view(UserType.ADMIN))
                .containsExactly(new TopOrderedItem(itemId, "item", 0));
    }

    @Test
    void givenItemsOrderedEvent_whenViewTopOrderedItems_thenThatItemsHaveCorrectTimesOrdered() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(itemId, userId, "item", 1));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(userId, itemId, 1, 1));
        module.onItemsOrderedEventUseCase().onEvent(new ItemsOrdered(userId, 2));

        assertThat(module.viewTopOrderedItemsUseCase().view(UserType.ADMIN))
                .containsExactly(new TopOrderedItem(itemId, "item", 1));
    }
}
