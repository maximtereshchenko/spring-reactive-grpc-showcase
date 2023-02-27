package com.github.xini1.orders.write.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.Event;
import com.github.xini1.common.event.EventType;
import com.github.xini1.common.event.cart.CartEvent;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemActivated;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.common.event.item.ItemEvent;
import com.github.xini1.common.mongodb.EventDocument;
import com.github.xini1.orders.write.port.EventStore;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Maxim Tereshchenko
 */
final class MongoEventStore implements EventStore {

    private final EventRepository eventRepository;
    private final NotificationMessagingTemplate notificationMessagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
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

    MongoEventStore(EventRepository eventRepository, NotificationMessagingTemplate notificationMessagingTemplate) {
        this.eventRepository = eventRepository;
        this.notificationMessagingTemplate = notificationMessagingTemplate;
    }

    @Override
    public void publish(Event event) {
        var json = json(event);
        eventRepository.save(new EventDocument(event, json))
                .subscribe();
        notificationMessagingTemplate.convertAndSend(Shared.EVENTS_SNS_TOPIC, json);
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
                .apply(properties(eventDocument));
    }

    private ItemEvent itemEvent(EventDocument eventDocument) {
        return itemEventConstructors.get(eventDocument.getEventType())
                .apply(properties(eventDocument));
    }

    private String json(Event event) {
        try {
            return objectMapper.writeValueAsString(new TreeMap<>(event.asMap()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not write JSON", e);
        }
    }

    private Map<String, String> properties(EventDocument eventDocument) {
        try {
            return objectMapper.readValue(eventDocument.getData(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read JSON", e);
        }
    }
}
