package com.github.xini1.event;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
public interface Event {

    UUID aggregateId();

    EventType type();

    long version();
}
