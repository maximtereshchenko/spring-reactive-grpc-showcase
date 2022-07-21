package com.github.xini1.users.application;

import com.github.xini1.common.*;
import com.github.xini1.common.event.*;
import com.github.xini1.common.mongodb.*;
import com.google.gson.*;
import org.apache.kafka.clients.producer.*;
import reactor.core.publisher.*;
import reactor.kafka.sender.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class MongoEventStore implements BasicEventStore {

    private final EventRepository eventRepository;
    private final KafkaSender<UUID, String> kafkaSender;
    private final Gson gson = new Gson();

    MongoEventStore(EventRepository eventRepository, KafkaSender<UUID, String> kafkaSender) {
        this.eventRepository = eventRepository;
        this.kafkaSender = kafkaSender;
    }

    @Override
    public void publish(Event event) {
        var json = gson.toJson(new TreeMap<>(event.asMap()));
        eventRepository.save(new EventDocument(event, json))
                .subscribe();
        kafkaSender.send(
                        Mono.just(
                                SenderRecord.create(
                                        new ProducerRecord<>(
                                                Shared.EVENTS_KAFKA_TOPIC,
                                                event.aggregateId(),
                                                json
                                        ),
                                        event.aggregateId()
                                )
                        )
                )
                .subscribe();
    }
}
