package com.github.xini1.orders.write.domain;

import com.github.xini1.common.*;
import com.github.xini1.orders.write.exception.*;
import com.github.xini1.orders.write.port.*;
import com.github.xini1.orders.write.usecase.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class ItemService implements CreateItemUseCase, DeactivateItemUseCase, ActivateItemUseCase {

    private final EventStore eventStore;

    ItemService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public UUID create(UUID userId, UserType userType, String name) throws UserIsNotAdmin {
        if (userType != UserType.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.create(userId, name);
        item.save(eventStore);
        return item.id();
    }

    @Override
    public void deactivate(UUID userId, UserType userType, UUID itemId)
            throws UserIsNotAdmin, ItemIsAlreadyDeactivated, ItemIsNotFound {
        if (userType != UserType.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.fromEvents(itemId, eventStore);
        item.deactivate(userId);
        item.save(eventStore);
    }

    @Override
    public void activate(UUID userId, UserType userType, UUID itemId)
            throws UserIsNotAdmin, ItemIsNotFound, ItemIsAlreadyActive {
        if (userType != UserType.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.fromEvents(itemId, eventStore);
        item.activate(userId);
        item.save(eventStore);
    }
}
