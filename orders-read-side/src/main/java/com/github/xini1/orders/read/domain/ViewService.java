package com.github.xini1.orders.read.domain;

import com.github.xini1.common.*;
import com.github.xini1.orders.read.exception.*;
import com.github.xini1.orders.read.port.*;
import com.github.xini1.orders.read.usecase.*;
import com.github.xini1.orders.read.view.*;

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
    public Cart view(UUID userId, UserType userType) {
        if (userType != UserType.REGULAR) {
            throw new UserIsNotRegular();
        }
        return viewStore.findCart(userId);
    }

    @Override
    public Iterable<Item> view() {
        return viewStore.findAllItems();
    }

    @Override
    public Iterable<TopOrderedItem> view(UserType userType) {
        if (userType != UserType.ADMIN) {
            throw new UserIsNotAdmin();
        }
        return viewStore.findAllTopOrderedItems();
    }

    @Override
    public OrderedItems viewOrderedItems(UUID userId, UserType userType) {
        if (userType != UserType.REGULAR) {
            throw new UserIsNotRegular();
        }
        return viewStore.findOrderedItems(userId);
    }
}
