package com.github.xini1;

import com.github.xini1.domain.Module;
import com.github.xini1.event.cart.*;
import com.github.xini1.event.item.*;
import com.github.xini1.exception.*;
import com.github.xini1.view.*;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Maxim Tereshchenko
 */
final class ViewOrderedItemsUseCaseTest {

    private final Clock clock = Clock.fixed(Instant.MIN, ZoneOffset.UTC);
    private final Module module = new Module(new InMemoryViewStore(), clock);
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenUserIsNotRegular_whenViewOrderedItems_thenUserIsNotRegularThrown() {
        var useCase = module.viewOrderedItemsUseCase();

        assertThatThrownBy(() -> useCase.viewOrderedItems(userId, User.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);
    }

    @Test
    void givenUserDidNotOrderAnyItem_whenViewOrderedItems_thenOrderedItemsIsEmpty() {
        assertThat(module.viewOrderedItemsUseCase().viewOrderedItems(userId, User.REGULAR))
                .isEqualTo(new OrderedItems(userId));
    }

    @Test
    void givenItemsOrderedEvent_whenViewOrderedItems_thenOrderedItemsHaveExactlyTheseItems() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(1, userId, itemId, 1));
        module.onItemsOrderedEventUseCase().onEvent(new ItemsOrdered(2, userId));

        assertThat(module.viewOrderedItemsUseCase().viewOrderedItems(userId, User.REGULAR))
                .isEqualTo(
                        new OrderedItems(
                                userId,
                                new OrderedItems.Order(
                                        clock.instant(),
                                        new OrderedItems.ItemInOrder(itemId, 1)
                                )
                        )
                );
    }
}
