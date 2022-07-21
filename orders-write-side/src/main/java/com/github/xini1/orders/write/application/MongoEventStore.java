package com.github.xini1.orders.write.application;

import com.github.xini1.common.*;
import com.github.xini1.common.event.*;
import com.github.xini1.common.event.cart.*;
import com.github.xini1.common.event.item.*;
import com.github.xini1.orders.write.port.*;
import com.google.gson.*;
import com.google.gson.reflect.*;
import org.apache.kafka.clients.producer.*;
import reactor.core.publisher.*;
import reactor.kafka.sender.*;

import java.util.*;
import java.util.function.*;

/**
 * @author Maxim Tereshchenko
 */
final class MongoEventStore implements EventStore {

    private final EventRepository eventRepository;
    private final KafkaSender<UUID, String> kafkaSender;
    private final Gson gson = new Gson();
    private final Map<EventType, Function<Map<String, String>, ItemEvent>> itemEventConstructors = Map.of(
            EventType.ITEM_ACTIVATED, ItemActivated::new,
            EventType.ITEM_DEACTIVATED, ItemDeactivated::new,
            EventType.ITEM_CREATED, ItemCreated::new
    );
    private final Map<EventType, Function<Map<String, String>, CartEvent>> cartEventConstructors = Map.of(
            EventType.ITEM_ADDED_TO_CART, ItemAddedToCart::new,
            EventType.ITEM_REMOVED_FROM_CART, ItemRemovedFromCart::new,
            EventType.ITEMS_ORDERED, ItemsOrdered::new
    );

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

    @Override
    public List<ItemEvent> itemEvents(UUID itemId) {
        return eventRepository.findById_AggregateIdAndEventTypeIn(itemId, EventType.itemEvents())
                .map(this::itemEvent)
                .collectList()
                .block();
    }

    @Override
    public List<CartEvent> cartEvents(UUID userId) {
        return eventRepository.findById_AggregateIdAndEventTypeIn(userId, EventType.cartEvents())
                .map(this::cartEvent)
                .collectList()
                .block();
    }

    private CartEvent cartEvent(EventDocument eventDocument) {
        return cartEventConstructors.get(eventDocument.getEventType())
                .apply(
                        gson.fromJson(
                                eventDocument.getData(),
                                new TypeToken<Map<String, String>>() {}.getType()
                        )
                );
    }

    private ItemEvent itemEvent(EventDocument eventDocument) {
        return itemEventConstructors.get(eventDocument.getEventType())
                .apply(
                        gson.fromJson(
                                eventDocument.getData(),
                                new TypeToken<Map<String, String>>() {}.getType()
                        )
                );
    }
}
