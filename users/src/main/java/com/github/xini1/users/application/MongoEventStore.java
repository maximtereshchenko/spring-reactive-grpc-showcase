package com.github.xini1.users.application;

import com.github.xini1.common.event.*;

/**
 * @author Maxim Tereshchenko
 */
final class MongoEventStore implements BasicEventStore {

    private final EventRepository eventRepository;

    MongoEventStore(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void publish(Event event) {
        eventRepository.save(new EventDocument(event))
                .subscribe();
    }
}
