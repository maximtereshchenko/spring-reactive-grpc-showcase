package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.xini1.domain.Module;
import org.junit.jupiter.api.Test;

/**
 * @author Maxim Tereshchenko
 */
final class ViewItemsUseCaseTest {

    private final Module module = new Module(new InMemoryViewStore());

    @Test
    void givenNoItemCreatedEvent_whenViewItems_thenIterableIsEmpty() {
        assertThat(module.viewItemsUseCase().view()).isEmpty();
    }
}
