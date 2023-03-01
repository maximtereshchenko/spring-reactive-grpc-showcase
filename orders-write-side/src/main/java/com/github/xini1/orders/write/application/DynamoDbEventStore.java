package com.github.xini1.orders.write.application;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xini1.common.Shared;
import com.github.xini1.common.dynamodb.EventsSchema;
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
import com.github.xini1.orders.write.port.EventStore;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Maxim Tereshchenko
 */
final class DynamoDbEventStore implements EventStore {

    private final AmazonDynamoDB amazonDynamoDB;
    private final NotificationMessagingTemplate notificationMessagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventsSchema eventsSchema = new EventsSchema();
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

    DynamoDbEventStore(AmazonDynamoDB amazonDynamoDB, NotificationMessagingTemplate notificationMessagingTemplate) {
        this.amazonDynamoDB = amazonDynamoDB;
        this.notificationMessagingTemplate = notificationMessagingTemplate;
    }

    @Override
    public void publish(Event event) {
        var json = json(event);
        amazonDynamoDB.putItem(eventsSchema.putRequest(event, json));
        notificationMessagingTemplate.convertAndSend(Shared.EVENTS_SNS_TOPIC, json);
    }

    @Override
    public List<ItemEvent> itemEvents(UUID itemId) {
        return amazonDynamoDB.scan(eventsSchema.findByAggregateIdAndEventTypeInRequest(itemId, EventType.itemEvents()))
                .getItems()
                .stream()
                .map(eventsSchema::bind)
                .map(this::itemEvent)
                .collect(Collectors.toList());
    }

    @Override
    public List<CartEvent> cartEvents(UUID userId) {
        return amazonDynamoDB.scan(eventsSchema.findByAggregateIdAndEventTypeInRequest(userId, EventType.cartEvents()))
                .getItems()
                .stream()
                .map(eventsSchema::bind)
                .map(this::cartEvent)
                .collect(Collectors.toList());
    }

    private CartEvent cartEvent(EventsSchema.Binding binding) {
        return cartEventConstructors.get(binding.eventType())
                .apply(properties(binding.data()));
    }

    private ItemEvent itemEvent(EventsSchema.Binding binding) {
        return itemEventConstructors.get(binding.eventType())
                .apply(properties(binding.data()));
    }

    private String json(Event event) {
        try {
            return objectMapper.writeValueAsString(new TreeMap<>(event.asMap()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not write JSON", e);
        }
    }

    private Map<String, String> properties(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read JSON", e);
        }
    }
}
