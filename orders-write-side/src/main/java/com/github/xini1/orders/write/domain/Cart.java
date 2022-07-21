package com.github.xini1.orders.write.domain;

import com.github.xini1.common.event.cart.*;
import com.github.xini1.orders.write.exception.*;
import com.github.xini1.orders.write.port.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class Cart extends AggregateRoot {

    private final Map<UUID, Integer> items = new HashMap<>();

    Cart(UUID userId) {
        super(userId);
        register(ItemAddedToCart.class, this::onEvent);
        register(ItemsOrdered.class, event -> items.clear());
        register(ItemRemovedFromCart.class, this::onEvent);
    }

    static Cart fromEvents(UUID userId, EventStore eventStore) {
        var cart = new Cart(userId);
        eventStore.cartEvents(userId)
                .forEach(cart::apply);
        cart.clearEvents();
        return cart;
    }

    void add(Item item, int quantity) {
        if (item.isDeactivated()) {
            throw new CouldNotAddDeactivatedItemToCart();
        }
        if (quantity < 1) {
            throw new QuantityIsNotPositive();
        }
        apply(new ItemAddedToCart(id(), item.id(), quantity, nextVersion()));
    }

    void remove(Item item, int quantity) {
        if (quantity < 1) {
            throw new QuantityIsNotPositive();
        }
        if (quantity > items.getOrDefault(item.id(), 0)) {
            throw new QuantityIsMoreThanCartHas();
        }
        apply(new ItemRemovedFromCart(id(), item.id(), quantity, nextVersion()));
    }

    void order(EventStore eventStore) {
        if (items.isEmpty()) {
            throw new CartIsEmpty();
        }
        if (hasDeactivatedItem(eventStore)) {
            throw new CartHasDeactivatedItem();
        }
        apply(new ItemsOrdered(id(), nextVersion()));
    }

    private boolean hasDeactivatedItem(EventStore eventStore) {
        return items.keySet()
                .stream()
                .map(id -> Item.fromEvents(id, eventStore))
                .anyMatch(Item::isDeactivated);
    }

    private Integer sum(Integer oldQuantity, int quantity) {
        if (oldQuantity == null) {
            return quantity;
        }
        return oldQuantity + quantity;
    }

    private void onEvent(ItemAddedToCart itemAddedToCart) {
        items.compute(itemAddedToCart.itemId(), (id, oldQuantity) -> sum(oldQuantity, itemAddedToCart.quantity()));
    }

    private void onEvent(ItemRemovedFromCart itemRemovedFromCart) {
        items.compute(
                itemRemovedFromCart.itemId(),
                (id, oldQuantity) -> Objects.requireNonNull(oldQuantity) - itemRemovedFromCart.quantity()
        );
    }
}
