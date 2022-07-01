package com.github.xini1.domain;

import com.github.xini1.usecase.AddItemUseCase;
import com.github.xini1.usecase.EventStore;
import com.github.xini1.usecase.Identifiers;
import com.github.xini1.usecase.ItemAdded;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ItemService implements AddItemUseCase {

    private final EventStore eventStore;
    private final Identifiers identifiers;

    ItemService(EventStore eventStore, Identifiers identifiers) {
        this.eventStore = eventStore;
        this.identifiers = identifiers;
    }

    @Override
    public void addItem(UUID userId, User user, String name) {
        if (user == User.ADMIN) {
            eventStore.publish(new ItemAdded(userId, identifiers.newIdentifier(), name));
        }
    }
}
