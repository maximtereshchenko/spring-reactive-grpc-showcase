package com.github.xini1.port;

import com.github.xini1.domain.Item;
import com.github.xini1.view.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface ViewStore {

    Optional<Item> findItem(UUID itemId);

    Cart findCart(UUID userId);

    void save(Item item);

    void save(Cart cart);
}
