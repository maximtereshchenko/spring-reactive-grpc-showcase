package com.github.xini1.domain;

import com.github.xini1.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Maxim Tereshchenko
 */
abstract class AggregateRoot {

    private final List<Event> newEvents = new ArrayList<>();
    private final Map<Class<? extends Event>, Consumer<Event>> handlers = new HashMap<>();
    private long version = 0;

    <T extends Event> void register(Class<T> eventType, Consumer<T> handler) {
        handlers.put(eventType, event -> handler.accept(eventType.cast(event)));
    }

    long nextVersion() {
        return ++version;
    }

    void apply(Event event) {
        handlers.get(event.getClass()).accept(event);
        newEvents.add(event);
    }

    void clearEvents() {
        newEvents.clear();
    }

    List<Event> newEvents() {
        return List.copyOf(newEvents);
    }
}
