package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.xini1.domain.Module;
import com.github.xini1.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.usecase.ItemAddedToCart;
import com.github.xini1.usecase.ItemCreated;
import com.github.xini1.usecase.ItemDeactivated;
import com.github.xini1.usecase.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class RegularUserUseCasesTest {

    private final UUID itemId = UUID.fromString("00000000-000-0000-0000-000000000001");
    private final UUID userId = UUID.fromString("00000000-000-0000-0000-000000000002");
    private final InMemoryEventStore eventStore = new InMemoryEventStore();
    private final Module module = new Module.Builder()
            .with(eventStore)
            .with(new StaticIdentifiers(itemId))
            .build();

    @Test
    void givenUserIsAdmin_whenAddItemToCart_thenUserIsNotRegularThrown() {
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, User.ADMIN, itemId))
                .isInstanceOf(UserIsNotRegular.class);

        assertThat(eventStore.events()).isEmpty();
    }

    @Test
    void givenDeactivatedItem_whenAddItemToCart_thenCouldNotAddDeactivatedItemToCartThrown() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");
        module.deactivateItemUseCase().deactivate(userId, User.ADMIN, itemId);
        var useCase = module.addItemToCartUseCase();

        assertThatThrownBy(() -> useCase.add(userId, User.REGULAR, itemId))
                .isInstanceOf(CouldNotAddDeactivatedItemToCart.class);

        assertThat(eventStore.events())
                .containsExactly(new ItemCreated(userId, itemId, "item"), new ItemDeactivated(userId, itemId));
    }

    @Test
    void givenActivateItem_whenAddItemToCart_thenItemAddedToCartEventPublished() {
        module.createItemUseCase().create(userId, User.ADMIN, "item");

        module.addItemToCartUseCase().add(userId, User.REGULAR, itemId);

        assertThat(eventStore.events())
                .containsExactly(
                        new ItemCreated(userId, itemId, "item"),
                        new ItemAddedToCart(userId, itemId)
                );
    }
}
