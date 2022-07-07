package com.github.xini1.orders.read;

import com.github.xini1.orders.read.port.*;
import com.github.xini1.orders.read.view.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryViewStore implements ViewStore {

    private final Map<UUID, Item> items = new HashMap<>();
    private final Map<UUID, Cart> carts = new HashMap<>();
    private final Map<UUID, TopOrderedItem> topOrderedItems = new HashMap<>();
    private final Map<UUID, OrderedItems> orderedItems = new HashMap<>();

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
    public Collection<Cart> findCartsByItemIdAndItemVersionLess(UUID itemId, long version) {
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

    @Override
    public Iterable<TopOrderedItem> findAllTopOrderedItems() {
        return topOrderedItems.values()
                .stream()
                .sorted(Comparator.comparingLong(TopOrderedItem::getTimesOrdered).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public TopOrderedItem findTopOrderedItem(UUID itemId) {
        return topOrderedItems.get(itemId);
    }

    @Override
    public void save(TopOrderedItem topOrderedItem) {
        topOrderedItems.put(topOrderedItem.getId(), topOrderedItem);
    }

    @Override
    public OrderedItems findOrderedItems(UUID userId) {
        var items = orderedItems.get(userId);
        if (items == null) {
            return new OrderedItems(userId);
        }
        return items;
    }

    @Override
    public void save(OrderedItems orderedItems) {
        this.orderedItems.put(orderedItems.getUserId(), orderedItems);
    }
}
