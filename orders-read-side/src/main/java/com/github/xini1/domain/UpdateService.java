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
        var cart = viewStore.findCart(itemAddedToCart.aggregateId());
        if (cart.hasVersionGreaterOrEqualTo(itemAddedToCart.version())) {
            return;
        }
        viewStore.save(
                cart.with(
                        viewStore.findItem(itemAddedToCart.itemId()),
                        itemAddedToCart.quantity(),
                        itemAddedToCart.version()
                )
        );
    }

    @Override
    public void onEvent(ItemCreated itemCreated) {
        viewStore.save(new Item(itemCreated));
    }

    @Override
    public void onEvent(ItemRemovedFromCart itemRemovedFromCart) {
        var cart = viewStore.findCart(itemRemovedFromCart.aggregateId());
        if (cart.hasVersionGreaterOrEqualTo(itemRemovedFromCart.version())) {
            return;
        }
        viewStore.save(
                cart.without(
                        viewStore.findItem(itemRemovedFromCart.itemId()),
                        itemRemovedFromCart.quantity(),
                        itemRemovedFromCart.version()
                )
        );
    }

    @Override
    public void onEvent(ItemsOrdered itemsOrdered) {
        var cart = viewStore.findCart(itemsOrdered.aggregateId());
        if (cart.hasVersionGreaterOrEqualTo(itemsOrdered.version())) {
            return;
        }
        viewStore.save(new Cart(itemsOrdered.aggregateId(), itemsOrdered.version()));
    }

    @Override
    public void onEvent(ItemDeactivated itemDeactivated) {
        var found = viewStore.findItem(itemDeactivated.aggregateId());
        if (found.hasVersionLessThan(itemDeactivated.version())) {
            viewStore.save(found.deactivated(itemDeactivated.version()));
        }
        viewStore.findCartsByItemIdAndItemVersionGreater(itemDeactivated.aggregateId(), itemDeactivated.version())
                .stream()
                .map(cart -> cart.withDeactivated(found.deactivated(itemDeactivated.version())))
                .forEach(viewStore::save);
    }

    @Override
    public void onEvent(ItemActivated itemActivated) {
        var found = viewStore.findItem(itemActivated.aggregateId());
        if (found.hasVersionLessThan(itemActivated.version())) {
            viewStore.save(found.activated(itemActivated.version()));
        }
        viewStore.findCartsByItemIdAndItemVersionGreater(itemActivated.aggregateId(), itemActivated.version())
                .stream()
                .map(cart -> cart.withActivated(found.activated(itemActivated.version())))
                .forEach(viewStore::save);
    }
}
