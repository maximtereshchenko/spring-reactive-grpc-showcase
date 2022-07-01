package com.github.xini1;

import com.github.xini1.usecase.Event;
import com.github.xini1.usecase.EventStore;

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
    public List<Event> findById(UUID itemId) {
        return events.stream()
                .filter(event -> event.itemId().equals(itemId))
                .collect(Collectors.toList());
    }

    List<Event> events() {
        return events;
    }
}
