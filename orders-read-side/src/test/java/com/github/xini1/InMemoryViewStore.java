package com.github.xini1;

import com.github.xini1.domain.Item;
import com.github.xini1.port.ViewStore;
import com.github.xini1.view.Cart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryViewStore implements ViewStore {

    private final Map<UUID, Item> items = new HashMap<>();
    private final Map<UUID, Cart> carts = new HashMap<>();

    @Override
    public Optional<Item> findItem(UUID itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Cart findCart(UUID userId) {
        var cart = carts.get(userId);
        if (cart == null) {
            return new Cart(userId);
        }
        return cart;
    }

    @Override
    public void save(Item item) {
        items.put(item.getId(), item);
    }

    @Override
    public void save(Cart cart) {
        carts.put(cart.getUserId(), cart);
    }

    @Override
    public Iterable<Item> items() {
        return items.values();
    }
}
