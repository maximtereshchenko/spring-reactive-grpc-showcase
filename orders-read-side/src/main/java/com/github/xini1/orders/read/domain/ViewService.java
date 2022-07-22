package com.github.xini1.orders.read.domain;

import com.github.xini1.common.UserType;
import com.github.xini1.orders.read.exception.UserIsNotAdmin;
import com.github.xini1.orders.read.exception.UserIsNotRegular;
import com.github.xini1.orders.read.port.ViewStore;
import com.github.xini1.orders.read.usecase.ViewCartUseCase;
import com.github.xini1.orders.read.usecase.ViewItemsUseCase;
import com.github.xini1.orders.read.usecase.ViewOrderedItemsUseCase;
import com.github.xini1.orders.read.usecase.ViewTopOrderedItemsUseCase;
import com.github.xini1.orders.read.view.Cart;
import com.github.xini1.orders.read.view.Item;
import com.github.xini1.orders.read.view.OrderedItems;
import com.github.xini1.orders.read.view.TopOrderedItem;

import java.util.UUID;

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
    public Cart view(UUID userId, UserType userType) throws UserIsNotRegular {
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
    public Iterable<TopOrderedItem> view(UserType userType) throws UserIsNotAdmin {
        if (userType != UserType.ADMIN) {
            throw new UserIsNotAdmin();
        }
        return viewStore.findAllTopOrderedItems();
    }

    @Override
    public OrderedItems viewOrderedItems(UUID userId, UserType userType) throws UserIsNotRegular {
        if (userType != UserType.REGULAR) {
            throw new UserIsNotRegular();
        }
        return viewStore.findOrderedItems(userId);
    }
}
