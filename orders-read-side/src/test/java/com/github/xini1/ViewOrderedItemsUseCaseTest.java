package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.exception.UserIsNotRegular;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewOrderedItemsUseCaseTest {

    private final Module module = new Module(new InMemoryViewStore());
    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000002");

    @Test
    void givenUserIsNotRegular_whenViewOrderedItems_thenUserIsNotRegularThrown() {
        var useCase = module.viewOrderedItemsUseCase();

        assertThatThrownBy(() -> useCase.viewOrderedItems(userId, User.ADMIN))
                .isInstanceOf(UserIsNotRegular.class);
    }

    @Test
    void givenUserWasNotOrderAnyItem_whenViewOrderedItems_thenOrderedItemsIsEmpty() {

    }
}
