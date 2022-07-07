package com.github.xini1.common.event;

/**
 * @author Maxim Tereshchenko
 */
public interface BasicEventStore {

    void publish(Event event);
}
