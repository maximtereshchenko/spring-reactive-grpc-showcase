package com.github.xini1.domain;

import com.github.xini1.User;
import com.github.xini1.exception.UserIsNotRegular;
import com.github.xini1.port.ViewStore;
import com.github.xini1.usecase.ViewCartUseCase;
import com.github.xini1.usecase.ViewItemsUseCase;
import com.github.xini1.usecase.ViewTopOrderedItemsUseCase;
import com.github.xini1.view.Cart;
import com.github.xini1.view.Item;
import com.github.xini1.view.TopOrderedItem;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class ViewService implements ViewCartUseCase, ViewItemsUseCase, ViewTopOrderedItemsUseCase {

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
        return viewStore.findAllTopOrderedItems();
    }
}
