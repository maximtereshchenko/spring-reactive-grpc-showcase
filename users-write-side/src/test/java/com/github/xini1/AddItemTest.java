package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.xini1.domain.Module;
import com.github.xini1.usecase.AddItemUseCase;
import com.github.xini1.usecase.ItemAdded;
import com.github.xini1.usecase.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class AddItemTest {

    private final UUID itemId = UUID.randomUUID();
    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final AddItemUseCase useCase = new Module.Builder()
            .with(eventStore)
            .with(new StaticIdentifiers(itemId))
            .build()
            .addItemUseCase();

    @Test
    void givenUserIsAdmin_whenAddItem_thenItemAddedEventSaved() {
        var userId = UUID.randomUUID();
        useCase.addItem(userId, User.ADMIN, "item");
        assertThat(eventStore.events()).containsExactly(new ItemAdded(userId, itemId, "item"));
    }
}
