package com.github.xini1.orders.write.port;

import com.github.xini1.common.event.*;
import com.github.xini1.common.event.cart.*;
import com.github.xini1.common.event.item.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface EventStore {

    void publish(Event event);

    List<ItemEvent> itemEvents(UUID itemId);

    List<CartEvent> cartEvents(UUID userId);
}
