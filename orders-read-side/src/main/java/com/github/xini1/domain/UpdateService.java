package com.github.xini1.domain;

import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemRemovedFromCart;
import com.github.xini1.event.cart.ItemsOrdered;
import com.github.xini1.event.item.ItemCreated;
import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.port.ViewStore;
import com.github.xini1.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.usecase.OnItemCreatedEventUseCase;
import com.github.xini1.usecase.OnItemRemovedFromCartEventUseCase;
import com.github.xini1.usecase.OnItemsOrderedEventUseCase;
import com.github.xini1.view.Cart;

/**
 * @author Maxim Tereshchenko
 */
final class UpdateService implements OnItemCreatedEventUseCase, OnItemAddedToCartEventUseCase,
        OnItemRemovedFromCartEventUseCase, OnItemsOrderedEventUseCase {

    private final ViewStore viewStore;

    UpdateService(ViewStore viewStore) {
        this.viewStore = viewStore;
    }

    @Override
    public void onEvent(ItemAddedToCart itemAddedToCart) {
        viewStore.save(
                viewStore.findCart(itemAddedToCart.aggregateId())
                        .with(
                                viewStore.findItem(itemAddedToCart.itemId())
                                        .orElseThrow(ItemIsNotFound::new),
                                itemAddedToCart.quantity()
                        )
        );
    }

    @Override
    public void onEvent(ItemCreated itemCreated) {
        viewStore.save(new Item(itemCreated));
    }

    @Override
    public void onEvent(ItemRemovedFromCart itemRemovedFromCart) {
        viewStore.save(
                viewStore.findCart(itemRemovedFromCart.aggregateId())
                        .without(
                                viewStore.findItem(itemRemovedFromCart.itemId())
                                        .orElseThrow(ItemIsNotFound::new),
                                itemRemovedFromCart.quantity()
                        )
        );
    }

    @Override
    public void onEvent(ItemsOrdered itemsOrdered) {
        viewStore.save(new Cart(itemsOrdered.aggregateId()));
    }
}
