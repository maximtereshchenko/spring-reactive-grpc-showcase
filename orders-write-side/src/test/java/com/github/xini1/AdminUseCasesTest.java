package com.github.xini1;

import com.github.xini1.domain.Module;
import com.github.xini1.event.item.*;
import com.github.xini1.exception.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Maxim Tereshchenko
 */
final class AdminUseCasesTest {

    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module(eventStore);
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID nonExistentItemId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenUserIsAdmin_whenCreateItem_thenItemCreatedEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");

        assertThat(eventStore.events())
                .containsExactly(new ItemCreated(1, userId, itemId, "item"));
    }

    @Test
    void givenUserIsNotAdmin_whenCreateItem_thenUserIsNotAdminThrown() {
        var useCase = module.createItemUseCase();

        assertThatThrownBy(() -> useCase.create(userId, User.REGULAR, "item"))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenItemDoNotExist_whenDeactivateItem_thenItemIsNotFoundThrown() {
        var useCase = module.deactivateItemUseCase();

        assertThatThrownBy(() -> useCase.deactivate(userId, User.ADMIN, nonExistentItemId))
                .isInstanceOf(ItemIsNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenUserIsNotAdmin_whenDeactivateItem_thenUserIsNotAdminThrown() {
        var useCase = module.deactivateItemUseCase();

        assertThatThrownBy(() -> useCase.deactivate(userId, User.REGULAR, nonExistentItemId))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenDeactivatedItem_whenDeactivateItem_thenItemIsAlreadyDeactivatedThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
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
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");

        module.deactivateItemUseCase()
                .deactivate(userId, User.ADMIN, itemId);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemDeactivated(2, userId, itemId)
                );
    }

    @Test
    void givenItemDoNotExist_whenActivateItem_thenItemIsNotFoundThrown() {
        var useCase = module.activateItemUseCase();

        assertThatThrownBy(() -> useCase.activate(userId, User.ADMIN, nonExistentItemId))
                .isInstanceOf(ItemIsNotFound.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenUserIsNotAdmin_whenActivateItem_thenUserIsNotAdminThrown() {
        var useCase = module.activateItemUseCase();

        assertThatThrownBy(() -> useCase.activate(userId, User.REGULAR, nonExistentItemId))
                .isInstanceOf(UserIsNotAdmin.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenActiveItem_whenActivateItem_thenItemIsAlreadyActiveThrown() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        var useCase = module.activateItemUseCase();

        assertThatThrownBy(() -> useCase.activate(userId, User.ADMIN, itemId))
                .isInstanceOf(ItemIsAlreadyActive.class);

        assertThat(eventStore.events())
                .containsExactly(new ItemCreated(1, userId, itemId, "item"));
    }

    @Test
    void givenDeactivatedItem_whenActivateItem_thenItemActivatedEventPublished() {
        var itemId = module.createItemUseCase()
                .create(userId, User.ADMIN, "item");
        module.deactivateItemUseCase()
                .deactivate(userId, User.ADMIN, itemId);

        module.activateItemUseCase().activate(userId, User.ADMIN, itemId);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(1, userId, itemId, "item"),
                        new ItemDeactivated(2, userId, itemId),
                        new ItemActivated(3, userId, itemId)
                );
    }
}
