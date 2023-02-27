package com.github.xini1.orders.read.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.Event;
import com.github.xini1.common.event.EventType;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemActivated;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.common.event.user.UserRegistered;
import com.github.xini1.orders.read.domain.Module;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Maxim Tereshchenko
 */
final class SqsEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
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

    public SqsEventConsumer(Module module) {
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

    @SqsListener(Shared.ORDERS_READ_SIDE_SQS_QUEUE)
    void subscribe(@Payload String message) {
        consume(event(properties(message)));
    }

    private void consume(Event event) {
        handlers.getOrDefault(event.type(), ignored -> {})
                .accept(event);
    }

    private Event event(Map<String, String> properties) {
        return eventConstructors.get(EventType.valueOf(properties.get("eventType"))).apply(properties);
    }

    private Map<String, String> properties(String message) {
        try {
            return objectMapper.readValue(message, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read JSON", e);
        }
    }
}
