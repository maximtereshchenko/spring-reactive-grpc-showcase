package com.github.xini1.domain;

import com.github.xini1.*;
import com.github.xini1.exception.*;
import com.github.xini1.port.*;
import com.github.xini1.usecase.*;
import com.github.xini1.view.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class ViewService implements ViewCartUseCase, ViewItemsUseCase, ViewTopOrderedItemsUseCase,
        ViewOrderedItemsUseCase {

    private final ViewStore viewStore;

    ViewService(ViewStore viewStore) {
        this.viewStore = viewStore;
    }

    @Override
    public Cart view(UUID userId, User user) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
        return viewStore.findCart(userId);
    }

    @Override
    public Iterable<Item> view() {
        return viewStore.findAllItems();
    }

    @Override
    public Iterable<TopOrderedItem> view(User user) {
        if (user != User.ADMIN) {
            throw new UserIsNotAdmin();
        }
        return viewStore.findAllTopOrderedItems();
    }

    @Override
    public OrderedItems viewOrderedItems(UUID userId, User user) {
        if (user != User.REGULAR) {
            throw new UserIsNotRegular();
        }
        return viewStore.findOrderedItems(userId);
    }
}
