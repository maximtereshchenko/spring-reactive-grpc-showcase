package com.github.xini1.domain;

import com.github.xini1.exception.ItemNotFound;
import com.github.xini1.exception.UserIsNotAdmin;
import com.github.xini1.usecase.AddItemUseCase;
import com.github.xini1.usecase.DisablePurchasingOfItemUseCase;
import com.github.xini1.usecase.EventStore;
import com.github.xini1.usecase.Identifiers;
import com.github.xini1.usecase.ItemAdded;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ItemService implements AddItemUseCase, DisablePurchasingOfItemUseCase {

    private final EventStore eventStore;
    private final Identifiers identifiers;

    ItemService(EventStore eventStore, Identifiers identifiers) {
        this.eventStore = eventStore;
        this.identifiers = identifiers;
    }

    @Override
    public void addItem(UUID userId, User user, String name) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        eventStore.publish(new ItemAdded(userId, identifiers.newIdentifier(), name));
    }

    @Override
    public void disablePurchasing(UUID userId, User user, UUID itemId) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var events = eventStore.findById(itemId);
        if (events.isEmpty()) {
            throw new ItemNotFound();
        }
    }
}
