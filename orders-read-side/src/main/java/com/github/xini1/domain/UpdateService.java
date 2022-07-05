package com.github.xini1.domain;

import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.item.ItemCreated;
import com.github.xini1.exception.ItemIsNotFound;
import com.github.xini1.port.ViewStore;
import com.github.xini1.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.usecase.OnItemCreatedEventUseCase;

/**
 * @author Maxim Tereshchenko
 */
final class UpdateService implements OnItemCreatedEventUseCase, OnItemAddedToCartEventUseCase {

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
}
