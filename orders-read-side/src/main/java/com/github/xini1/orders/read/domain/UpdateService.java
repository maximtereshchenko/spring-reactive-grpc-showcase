package com.github.xini1.orders.read.domain;

import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemActivated;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.orders.read.port.ViewStore;
import com.github.xini1.orders.read.usecase.OnItemActivatedEventUseCase;
import com.github.xini1.orders.read.usecase.OnItemAddedToCartEventUseCase;
import com.github.xini1.orders.read.usecase.OnItemCreatedEventUseCase;
import com.github.xini1.orders.read.usecase.OnItemDeactivatedEventUseCase;
import com.github.xini1.orders.read.usecase.OnItemRemovedFromCartEventUseCase;
import com.github.xini1.orders.read.usecase.OnItemsOrderedEventUseCase;
import com.github.xini1.orders.read.view.Cart;
import com.github.xini1.orders.read.view.Item;
import com.github.xini1.orders.read.view.OrderedItems;
import com.github.xini1.orders.read.view.TopOrderedItem;

import java.time.Clock;
import java.util.stream.Collectors;

/**
 * @author Maxim Tereshchenko
 */
final class UpdateService implements OnItemCreatedEventUseCase, OnItemAddedToCartEventUseCase,
        OnItemRemovedFromCartEventUseCase, OnItemsOrderedEventUseCase, OnItemDeactivatedEventUseCase,
        OnItemActivatedEventUseCase {

    private final ViewStore viewStore;
    private final Clock clock;

    UpdateService(ViewStore viewStore, Clock clock) {
        this.viewStore = viewStore;
        this.clock = clock;
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
        var item = new Item(itemCreated);
        viewStore.save(item);
        viewStore.save(new TopOrderedItem(item));
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
        cart.getItemsInCart()
                .stream()
                .map(this::topOrderedItemWithAddedQuantity)
                .forEach(viewStore::save);
        viewStore.save(
                viewStore.findOrderedItems(itemsOrdered.aggregateId())
                        .withOrder(
                                new OrderedItems.Order(
                                        clock.instant(),
                                        cart.getItemsInCart()
                                                .stream()
                                                .map(OrderedItems.ItemInOrder::new)
                                                .collect(Collectors.toList())
                                )
                        )
        );
    }

    @Override
    public void onEvent(ItemDeactivated itemDeactivated) {
        var found = viewStore.findItem(itemDeactivated.aggregateId());
        if (found.hasVersionLessThan(itemDeactivated.version())) {
            viewStore.save(found.deactivated(itemDeactivated.version()));
        }
        viewStore.findCartsByItemIdAndItemVersionLess(itemDeactivated.aggregateId(), itemDeactivated.version())
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
        viewStore.findCartsByItemIdAndItemVersionLess(itemActivated.aggregateId(), itemActivated.version())
                .stream()
                .map(cart -> cart.withActivated(found.activated(itemActivated.version())))
                .forEach(viewStore::save);
    }

    private TopOrderedItem topOrderedItemWithAddedQuantity(Cart.ItemInCart itemInCart) {
        return viewStore.findTopOrderedItem(itemInCart.getId()).addTimesOrdered(itemInCart.getQuantity());
    }
}
