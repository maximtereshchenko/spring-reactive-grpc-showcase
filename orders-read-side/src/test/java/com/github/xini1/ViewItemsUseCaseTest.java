package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.xini1.domain.Module;
import com.github.xini1.event.item.ItemActivated;
import com.github.xini1.event.item.ItemCreated;
import com.github.xini1.event.item.ItemDeactivated;
import com.github.xini1.view.Item;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewItemsUseCaseTest {

    private final Module module = new Module(new InMemoryViewStore());
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenNoItemCreatedEvent_whenViewItems_thenIterableIsEmpty() {
        assertThat(module.viewItemsUseCase().view()).isEmpty();
    }

    @Test
    void givenItemCreatedEvent_whenViewItems_thenIterableHasThatItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));

        assertThat(module.viewItemsUseCase().view())
                .containsExactly(new Item(itemId, "item", true, 1));
    }

    @Test
    void givenItemDeactivatedEvent_whenViewItems_thenIterableHasDeactivatedItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemDeactivatedEventUseCase().onEvent(new ItemDeactivated(2, userId, itemId));

        assertThat(module.viewItemsUseCase().view())
                .containsExactly(new Item(itemId, "item", false, 2));
    }

    @Test
    void givenItemActivatedEvent_whenViewItems_thenIterableHasActivatedItem() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemDeactivatedEventUseCase().onEvent(new ItemDeactivated(2, userId, itemId));
        module.onItemActivatedEventUseCase().onEvent(new ItemActivated(3, userId, itemId));

        assertThat(module.viewItemsUseCase().view())
                .containsExactly(new Item(itemId, "item", true, 3));
    }

    @Test
    void givenItemHasVersionGreaterOrEqualToItemDeactivatedEventVersion_whenViewItems_thenItemWasNotChanged() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemDeactivatedEventUseCase().onEvent(new ItemDeactivated(1, userId, itemId));

        assertThat(module.viewItemsUseCase().view())
                .containsExactly(new Item(itemId, "item", true, 1));
    }

    @Test
    void givenItemHasVersionGreaterOrEqualToItemActivatedEventVersion_whenViewItems_thenItemWasNotChanged() {
        module.onItemCreatedEventUseCase().onEvent(new ItemCreated(1, userId, itemId, "item"));
        module.onItemDeactivatedEventUseCase().onEvent(new ItemDeactivated(2, userId, itemId));
        module.onItemActivatedEventUseCase().onEvent(new ItemActivated(2, userId, itemId));

        assertThat(module.viewItemsUseCase().view())
                .containsExactly(new Item(itemId, "item", false, 2));
    }
}
