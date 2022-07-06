package com.github.xini1.port;

import com.github.xini1.event.*;
import com.github.xini1.event.cart.*;
import com.github.xini1.event.item.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface EventStore {

    void publish(Event event);

    List<ItemEvent> itemEvents(UUID itemId);

    List<CartEvent> cartEvents(UUID userId);
}
