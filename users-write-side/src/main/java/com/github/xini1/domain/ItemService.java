package com.github.xini1.domain;

import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.exception.UserIsNotAdmin;
import com.github.xini1.port.Identifiers;
import com.github.xini1.usecase.ActivateItemUseCase;
import com.github.xini1.usecase.CreateItemUseCase;
import com.github.xini1.usecase.DeactivateItemUseCase;
import com.github.xini1.usecase.User;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ItemService implements CreateItemUseCase, DeactivateItemUseCase, ActivateItemUseCase {

    private final Items items;
    private final Identifiers identifiers;

    ItemService(Items items, Identifiers identifiers) {
        this.items = items;
        this.identifiers = identifiers;
    }

    @Override
    public UUID create(UUID userId, User user, String name) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = Item.create(userId, name, identifiers);
        items.save(item);
        return item.id();
    }

    @Override
    public void deactivate(UUID userId, User user, UUID itemId) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = items.find(itemId)
                .orElseThrow(ItemIsNotFound::new);
        item.deactivate(userId);
        items.save(item);
    }

    @Override
    public void activate(UUID userId, User user, UUID itemId) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        var item = items.find(itemId)
                .orElseThrow(ItemIsNotFound::new);
        item.activate(userId);
        items.save(item);
    }
}
