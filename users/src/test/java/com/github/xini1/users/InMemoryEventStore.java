package com.github.xini1.users;

import com.github.xini1.common.event.*;
import com.github.xini1.users.port.*;

import java.util.*;

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
