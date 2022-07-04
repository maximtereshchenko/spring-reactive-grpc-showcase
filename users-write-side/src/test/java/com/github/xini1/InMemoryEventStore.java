package com.github.xini1;

import com.github.xini1.usecase.CartEvent;
import com.github.xini1.usecase.Event;
import com.github.xini1.usecase.EventStore;
import com.github.xini1.usecase.ItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return events.stream()
                .filter(event -> ItemEvent.class.isAssignableFrom(event.getClass()))
                .map(ItemEvent.class::cast)
                .filter(event -> event.itemId().equals(itemId))
                .collect(Collectors.toList());
    }

    @Override
    public List<CartEvent> cartEvents(UUID userId) {
        return events.stream()
                .filter(event -> CartEvent.class.isAssignableFrom(event.getClass()))
                .map(CartEvent.class::cast)
                .filter(event -> event.userId().equals(userId))
                .collect(Collectors.toList());
    }

    List<Event> events() {
        return events;
    }
}
