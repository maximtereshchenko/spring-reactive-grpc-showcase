package com.github.xini1.users;

import com.github.xini1.common.event.BasicEventStore;
import com.github.xini1.common.event.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryEventStore implements BasicEventStore {

    private final List<Event> events = new ArrayList<>();

    @Override
    public void publish(Event event) {
        events.add(event);
    }

    List<Event> events() {
        return events;
    }
}
