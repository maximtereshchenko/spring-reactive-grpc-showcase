package com.github.xini1.domain;

import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemRemovedFromCart;
import com.github.xini1.event.cart.ItemsOrdered;
import com.github.xini1.event.item.ItemActivated;
import com.github.xini1.event.item.ItemCreated;
import com.github.xini1.event.item.ItemDeactivated;
import com.github.xini1.port.ViewStore;
import com.github.xini1.usecase.OnItemActivatedEventUseCase;
import com.github.xini1.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.usecase.OnItemCreatedEventUseCase;
import com.github.xini1.usecase.OnItemDeactivatedEventUseCase;
import com.github.xini1.usecase.OnItemRemovedFromCartEventUseCase;
import com.github.xini1.usecase.OnItemsOrderedEventUseCase;
import com.github.xini1.view.Cart;
import com.github.xini1.view.Item;

/**
 * @author Maxim Tereshchenko
 */
final class UpdateService implements OnItemCreatedEventUseCase, OnItemAddedToCartEventUseCase,
        OnItemRemovedFromCartEventUseCase, OnItemsOrderedEventUseCase, OnItemDeactivatedEventUseCase,
        OnItemActivatedEventUseCase {

    private final ViewStore viewStore;

    UpdateService(ViewStore viewStore) {
        this.viewStore = viewStore;
    }

    @Override
    public void onEvent(ItemAddedToCart itemAddedToCart) {
        viewStore.save(
                viewStore.findCart(itemAddedToCart.aggregateId())
                        .with(
                                viewStore.findItem(itemAddedToCart.itemId()),
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
                                viewStore.findItem(itemRemovedFromCart.itemId()),
                                itemRemovedFromCart.quantity()
                        )
        );
    }

    @Override
    public void onEvent(ItemsOrdered itemsOrdered) {
        viewStore.save(new Cart(itemsOrdered.aggregateId()));
    }

    @Override
    public void onEvent(ItemDeactivated itemDeactivated) {
        viewStore.save(viewStore.findItem(itemDeactivated.aggregateId()).deactivated());
    }

    @Override
    public void onEvent(ItemActivated itemActivated) {
        viewStore.save(viewStore.findItem(itemActivated.aggregateId()).activated());
    }
}
