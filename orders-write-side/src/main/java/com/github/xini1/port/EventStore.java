package com.github.xini1.port;

import com.github.xini1.event.Event;
import com.github.xini1.event.cart.CartEvent;
import com.github.xini1.event.item.ItemEvent;

import java.util.List;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface EventStore {

    void publish(Event event);

    List<ItemEvent> itemEvents(UUID itemId);

    List<CartEvent> cartEvents(UUID userId);
}
