package com.github.xini1;

import com.github.xini1.usecase.Event;
import com.github.xini1.usecase.EventStore;

import java.util.List;

/**
 * @author Maxim Tereshchenko
 */
final class InMemoryEventStore implements EventStore {
    List<Event> events() {
        return List.of();
    }
}
