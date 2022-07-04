package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.event.ItemActivated;
import com.github.xini1.event.ItemCreated;
import com.github.xini1.event.ItemDeactivated;
import com.github.xini1.exception.ItemIsAlreadyActive;
import com.github.xini1.exception.ItemIsAlreadyDeactivated;
import com.github.xini1.exception.ItemNotFound;
import com.github.xini1.exception.UserIsNotAdmin;
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
    void givenUserIsAdmin_whenCreateItem_thenItemCreatedEventPublished() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");

        assertThat(eventStore.events()).containsExactly(new ItemCreated(1, userId, itemId, "item"));
    }

    @Test
    void givenUserIsNotAdmin_whenCreateItem_thenUserIsNotAdminThrown() {
        var useCase = module.createItemUseCase();

        assertThatThrownBy(() -> useCase.create(userId, User.REGULAR, "item"))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenDeactivateItem_thenItemNotFoundThrown() {
        var useCase = module.deactivateItemUseCase();

        assertThatThrownBy(() -> useCase.deactivate(userId, User.ADMIN, itemId))
                .isInstanceOf(ItemNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenUserIsNotAdmin_whenDeactivateItem_thenUserIsNotAdminThrown() {
        var useCase = module.deactivateItemUseCase();

        assertThatThrownBy(() -> useCase.deactivate(userId, User.REGULAR, itemId))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenDeactivatedItem_whenDeactivateItem_thenItemIsAlreadyDeactivatedThrown() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");
        var useCase = module.deactivateItemUseCase();
        useCase.deactivate(userId, User.ADMIN, itemId);

        assertThatThrownBy(() -> useCase.deactivate(userId, User.ADMIN, itemId))
                .isInstanceOf(ItemIsAlreadyDeactivated.class);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemDeactivated(2, userId, itemId)
                );
    }

    @Test
    void givenActiveItem_whenDeactivateItem_thenItemDeactivatedEventPublished() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");

        module.deactivateItemUseCase().deactivate(userId, User.ADMIN, itemId);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemDeactivated(2, userId, itemId)
                );
    }

    @Test
    void givenItemDoNotExist_whenActivateItem_thenItemNotFoundThrown() {
        var useCase = module.activateItemUseCase();

        assertThatThrownBy(() -> useCase.activate(userId, User.ADMIN, itemId))
                .isInstanceOf(ItemNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenUserIsNotAdmin_whenActivateItem_thenUserIsNotAdminThrown() {
        var useCase = module.activateItemUseCase();

        assertThatThrownBy(() -> useCase.activate(userId, User.REGULAR, itemId))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenActiveItem_whenActivateItem_thenItemIsAlreadyActiveThrown() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");
        var useCase = module.activateItemUseCase();

        assertThatThrownBy(() -> useCase.activate(userId, User.ADMIN, itemId))
                .isInstanceOf(ItemIsAlreadyActive.class);

        assertThat(eventStore.events())
                .containsExactly(new ItemCreated(1, userId, itemId, "item"));
    }

    @Test
    void givenDeactivatedItem_whenActivateItem_thenItemActivatedEventPublished() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");
        module.deactivateItemUseCase().deactivate(userId, User.ADMIN, itemId);

        module.activateItemUseCase().activate(userId, User.ADMIN, itemId);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemDeactivated(2, userId, itemId),
                        new ItemActivated(3, userId, itemId)
                );
    }
}
