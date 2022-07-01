package com.github.xini1.usecase;

import java.util.List;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface EventStore {

    void publish(Event event);

    List<Event> findById(UUID itemId);
}
