package com.github.xini1.port;

import com.github.xini1.event.CartEvent;
import com.github.xini1.event.Event;
import com.github.xini1.event.ItemEvent;

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
