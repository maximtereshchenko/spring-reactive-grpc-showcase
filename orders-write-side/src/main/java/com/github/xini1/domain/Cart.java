package com.github.xini1.domain;

import com.github.xini1.event.cart.ItemAddedToCart;
import com.github.xini1.event.cart.ItemsOrdered;
import com.github.xini1.exception.CartHasDeactivatedItem;
import com.github.xini1.exception.CartIsEmpty;
import com.github.xini1.exception.CouldNotAddDeactivatedItemToCart;
import com.github.xini1.port.EventStore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class Cart extends AggregateRoot {

    private final Map<UUID, Integer> items = new HashMap<>();

    Cart(UUID userId) {
        super(userId);
        register(ItemAddedToCart.class, this::onEvent);
        register(ItemsOrdered.class, this::onEvent);
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
        apply(new ItemAddedToCart(nextVersion(), id(), item.id(), quantity));
    }

    void order(EventStore eventStore) {
        if (items.isEmpty()) {
            throw new CartIsEmpty();
        }
        if (hasDeactivatedItem(eventStore)) {
            throw new CartHasDeactivatedItem();
        }
        apply(new ItemsOrdered(nextVersion(), id(), items));
    }

    private boolean hasDeactivatedItem(EventStore eventStore) {
        return items.keySet()
                .stream()
                .map(id -> Item.fromEvents(id, eventStore))
                .anyMatch(Item::isDeactivated);
    }

    private void onEvent(ItemAddedToCart itemAddedToCart) {
        items.compute(itemAddedToCart.itemId(), (id, oldQuantity) -> sum(oldQuantity, itemAddedToCart.quantity()));
    }

    private Integer sum(Integer oldQuantity, int quantity) {
        if (oldQuantity == null) {
            return quantity;
        }
        return oldQuantity + quantity;
    }

    private void onEvent(ItemsOrdered itemsOrdered) {
        items.clear();
    }
}
