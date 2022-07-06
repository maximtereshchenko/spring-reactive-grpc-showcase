package com.github.xini1;

import com.github.xini1.port.ViewStore;
import com.github.xini1.view.Cart;
import com.github.xini1.view.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryViewStore implements ViewStore {

    private final Map<UUID, Item> items = new HashMap<>();
    private final Map<UUID, Cart> carts = new HashMap<>();

    @Override
    public Item findItem(UUID itemId) {
        return items.get(itemId);
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
    public Iterable<Item> findAllItems() {
        return items.values();
    }

    @Override
    public Collection<Cart> findCartsByItemIdAndItemVersionGreater(UUID itemId, long version) {
        return carts.values()
                .stream()
                .filter(cart ->
                        cart.getItemsInCart()
                                .stream()
                                .filter(itemInCart -> itemInCart.hasVersionLessThan(version))
                                .map(Cart.ItemInCart::getId)
                                .anyMatch(id -> id.equals(itemId))
                )
                .collect(Collectors.toList());
    }
}
