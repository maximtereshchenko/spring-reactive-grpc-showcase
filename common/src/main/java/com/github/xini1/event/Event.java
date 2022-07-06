package com.github.xini1.event;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public interface Event {

    UUID aggregateId();

    EventType type();

    long version();
}
