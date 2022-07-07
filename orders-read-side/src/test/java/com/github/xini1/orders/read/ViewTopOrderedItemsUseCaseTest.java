package com.github.xini1.orders.read;

import com.github.xini1.common.*;
import com.github.xini1.common.event.cart.*;
import com.github.xini1.common.event.item.*;
import com.github.xini1.orders.read.domain.Module;
import com.github.xini1.orders.read.exception.*;
import com.github.xini1.orders.read.view.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Maxim Tereshchenko
 */
final class ViewTopOrderedItemsUseCaseTest {

    private final Module module = new Module(new InMemoryViewStore());
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000002");

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
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));

        assertThat(module.viewTopOrderedItemsUseCase().view(UserType.ADMIN))
                .containsExactly(new TopOrderedItem(itemId, "item", 0));
    }

    @Test
    void givenItemsOrderedEvent_whenViewTopOrderedItems_thenThatItemsHaveCorrectTimesOrdered() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemsOrderedEventUseCase().onEvent(new ItemsOrdered(2, userId));

        assertThat(module.viewTopOrderedItemsUseCase().view(UserType.ADMIN))
                .containsExactly(new TopOrderedItem(itemId, "item", 1));
    }
}
