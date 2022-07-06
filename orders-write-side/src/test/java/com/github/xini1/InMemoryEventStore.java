package com.github.xini1;

import com.github.xini1.event.*;
import com.github.xini1.event.cart.*;
import com.github.xini1.event.item.*;
import com.github.xini1.port.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryEventStore implements EventStore {

    private final List<Event> events = new ArrayList<>();

    @Override
    public void publish(Event event) {
        events.add(event);
    }

    @Override
    public List<ItemEvent> itemEvents(UUID itemId) {
        return find(EventType.ITEM, itemId, ItemEvent.class);
    }

    @Override
    public List<CartEvent> cartEvents(UUID userId) {
        return find(EventType.CART, userId, CartEvent.class);
    }

    List<Event> events() {
        return events;
    }

    private <T extends Event> List<T> find(EventType eventType, UUID aggregateId, Class<T> resultType) {
        return events.stream()
                .filter(event -> event.type() == eventType)
                .filter(event -> event.aggregateId().equals(aggregateId))
                .sorted(Comparator.comparingLong(Event::version))
                .map(resultType::cast)
                .collect(Collectors.toList());
    }
}
