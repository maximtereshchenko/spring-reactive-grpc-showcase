package com.github.xini1.domain;

import com.github.xini1.exception.UserIsNotAdmin;
import com.github.xini1.port.EventStore;
import com.github.xini1.usecase.ActivateItemUseCase;
import com.github.xini1.usecase.CreateItemUseCase;
import com.github.xini1.usecase.DeactivateItemUseCase;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ItemService implements CreateItemUseCase, DeactivateItemUseCase, ActivateItemUseCase {

    private final EventStore eventStore;

    ItemService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public UUID create(UUID userId, User user, String name) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.create(userId, name);
        item.save(eventStore);
        return item.id();
    }

    @Override
    public void deactivate(UUID userId, User user, UUID itemId) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.fromEvents(itemId, eventStore);
        item.deactivate(userId);
        item.save(eventStore);
    }

    @Override
    public void activate(UUID userId, User user, UUID itemId) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.fromEvents(itemId, eventStore);
        item.activate(userId);
        item.save(eventStore);
    }
}
