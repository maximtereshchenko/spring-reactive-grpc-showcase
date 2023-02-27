package com.github.xini1.orders.read;

import com.github.xini1.common.UserType;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.orders.read.domain.Module;
import com.github.xini1.orders.read.exception.UserIsNotRegular;
import com.github.xini1.orders.read.view.OrderedItems;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Maxim Tereshchenko
 */
final class ViewOrderedItemsUseCaseTest {

    private final Clock clock = Clock.fixed(Instant.MIN, ZoneOffset.UTC);
    private final Module module = new Module(new InMemoryViewStore(), clock);
    private final UUID itemId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void givenUserIsNotRegular_whenViewOrderedItems_thenUserIsNotRegularThrown() {
        var useCase = module.viewOrderedItemsUseCase();

        assertThatThrownBy(() -> useCase.viewOrderedItems(userId, UserType.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);
    }

    @Test
    void givenUserDidNotOrderAnyItem_whenViewOrderedItems_thenOrderedItemsIsEmpty() {
        assertThat(module.viewOrderedItemsUseCase().viewOrderedItems(userId, UserType.REGULAR))
                .isEqualTo(new OrderedItems(userId));
    }

    @Test
    void givenItemsOrderedEvent_whenViewOrderedItems_thenOrderedItemsHaveExactlyTheseItems() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(itemId, userId, "item", 1));
        module.onItemAddedToCartEventUseCase().onEvent(new ItemAddedToCart(userId, itemId, 1, 1));
        module.onItemsOrderedEventUseCase().onEvent(new ItemsOrdered(userId, 2));

        assertThat(module.viewOrderedItemsUseCase().viewOrderedItems(userId, UserType.REGULAR))
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
