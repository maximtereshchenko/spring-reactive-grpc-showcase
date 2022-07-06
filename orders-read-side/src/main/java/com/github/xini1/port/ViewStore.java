package com.github.xini1.port;

import com.github.xini1.view.Cart;
import com.github.xini1.view.Item;
import com.github.xini1.view.TopOrderedItem;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewStore {

    Item findItem(UUID itemId);

    Cart findCart(UUID userId);

    void save(Item item);

    void save(Cart cart);

    Iterable<Item> findAllItems();

    Collection<Cart> findCartsByItemIdAndItemVersionLess(UUID itemId, long version);

    Iterable<TopOrderedItem> findAllTopOrderedItems();

    void save(TopOrderedItem topOrderedItem);
}
