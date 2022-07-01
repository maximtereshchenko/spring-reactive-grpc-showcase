package com.github.xini1;

import com.github.xini1.usecase.Event;
import com.github.xini1.usecase.EventStore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryEventStore implements EventStore {

    private final List<Event> events = new ArrayList<>();

    @Override
    public void publish(Event event) {
        events.add(event);
    }

    List<Event> events() {
        return events;
    }
}
