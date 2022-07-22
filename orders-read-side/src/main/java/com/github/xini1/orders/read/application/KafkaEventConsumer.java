package com.github.xini1.orders.read.application;

import com.github.xini1.common.event.*;
import com.github.xini1.common.event.cart.*;
import com.github.xini1.common.event.item.*;
import com.github.xini1.common.event.user.*;
import com.github.xini1.orders.read.domain.Module;
import com.google.gson.*;
import com.google.gson.reflect.*;
import org.apache.kafka.clients.consumer.*;
import reactor.kafka.receiver.*;

import javax.annotation.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.*;

/**
 * @author Maxim Tereshchenko
 */
final class KafkaEventConsumer {

    private final KafkaReceiver<UUID, String> kafkaReceiver;
    private final Gson gson = new Gson();
    private final Map<EventType, Consumer<Event>> handlers;
    private final Map<EventType, Function<Map<String, String>, Event>> eventConstructors = Map.of(
            EventType.ITEM_ACTIVATED, ItemActivated::new,
            EventType.ITEM_CREATED, ItemCreated::new,
            EventType.ITEM_DEACTIVATED, ItemDeactivated::new,
            EventType.USER_REGISTERED, UserRegistered::new,
            EventType.ITEM_ADDED_TO_CART, ItemAddedToCart::new,
            EventType.ITEM_REMOVED_FROM_CART, ItemRemovedFromCart::new,
            EventType.ITEMS_ORDERED, ItemsOrdered::new
    );

    public KafkaEventConsumer(KafkaReceiver<UUID, String> kafkaReceiver, Module module) {
        this.kafkaReceiver = kafkaReceiver;
        handlers = Map.of(
                EventType.ITEM_ACTIVATED,
                event -> module.onItemActivatedEventUseCase().onEvent((ItemActivated) event),
                EventType.ITEM_ADDED_TO_CART,
                event -> module.onItemAddedToCartEventUseCase().onEvent((ItemAddedToCart) event),
                EventType.ITEM_CREATED,
                event -> module.onItemCreatedEventUseCase().onEvent((ItemCreated) event),
                EventType.ITEM_DEACTIVATED,
                event -> module.onItemDeactivatedEventUseCase().onEvent((ItemDeactivated) event),
                EventType.ITEM_REMOVED_FROM_CART,
                event -> module.onItemRemovedFromCartEventUseCase().onEvent((ItemRemovedFromCart) event),
                EventType.ITEMS_ORDERED,
                event -> module.onItemsOrderedEventUseCase().onEvent((ItemsOrdered) event)
        );
    }

    @PostConstruct
    void subscribe() {
        kafkaReceiver.receive()
                .map(ConsumerRecord::value)
                .map(this::properties)
                .map(this::event)
                .doOnNext(this::consume)
                .subscribe();
    }

    private void consume(Event event) {
        handlers.getOrDefault(event.type(), ignored -> {
                })
                .accept(event);
    }

    private Event event(Map<String, String> properties) {
        return eventConstructors.get(EventType.valueOf(properties.get("eventType"))).apply(properties);
    }

    private Map<String, String> properties(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
