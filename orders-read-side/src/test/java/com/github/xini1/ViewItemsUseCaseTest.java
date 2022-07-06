package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.xini1.domain.Module;
import com.github.xini1.event.item.ItemCreated;
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

        assertThat(module.viewItemsUseCase().view()).containsExactly(new Item(itemId, "item"));
    }
}
