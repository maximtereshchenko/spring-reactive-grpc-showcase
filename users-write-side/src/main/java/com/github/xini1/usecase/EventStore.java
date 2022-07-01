package com.github.xini1.usecase;

/**
 * @author Maxim Tereshchenko
 */
public interface EventStore {

    void publish(Event event);
}
