package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.exception.UserIsNotAdmin;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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

        assertThatThrownBy(() -> useCase.view(User.REGULAR)).isInstanceOf(UserIsNotAdmin.class);
    }

    @Test
    void givenNoItemsOrdered_whenViewTopOrderedItems_thenIterableIsEmpty() {
        assertThat(module.viewTopOrderedItemsUseCase().view(User.ADMIN)).isEmpty();
    }
}
