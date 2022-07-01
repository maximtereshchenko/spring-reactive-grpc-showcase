package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.exception.ItemNotFound;
import com.github.xini1.exception.UserIsNotAdmin;
import com.github.xini1.usecase.ItemAdded;
import com.github.xini1.usecase.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class AdminUseCasesTest {

    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000002");
    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module.Builder()
            .with(eventStore)
            .with(new StaticIdentifiers(itemId))
            .build();

    @Test
    void givenUserIsAdmin_whenAddItem_thenItemAddedEventSaved() {
        module.addItemUseCase().addItem(userId, User.ADMIN, "item");

        assertThat(eventStore.events()).containsExactly(new ItemAdded(userId, itemId, "item"));
    }

    @Test
    void givenUserIsNotAdmin_whenAddItem_thenNoEventsSaved() {
        var useCase = module.addItemUseCase();

        assertThatThrownBy(() -> useCase.addItem(userId, User.REGULAR, "item"))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenDisablePurchasing_thenNoEventsSaved() {
        var useCase = module.disablePurchasingOfItemUseCase();

        assertThatThrownBy(() -> useCase.disablePurchasing(userId, User.ADMIN, itemId))
                .isInstanceOf(ItemNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenUserIsNotAdmin_whenDisablePurchasing_thenNoEventsSaved() {
        var useCase = module.disablePurchasingOfItemUseCase();

        assertThatThrownBy(() -> useCase.disablePurchasing(userId, User.REGULAR, itemId))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }
}
