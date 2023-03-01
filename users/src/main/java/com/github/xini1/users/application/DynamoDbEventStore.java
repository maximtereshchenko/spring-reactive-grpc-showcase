package com.github.xini1.users.application;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xini1.common.Shared;
import com.github.xini1.common.dynamodb.EventsSchema;
import com.github.xini1.common.event.BasicEventStore;
import com.github.xini1.common.event.Event;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;

import java.util.TreeMap;

/**
 * @author Maxim Tereshchenko
 */
final class DynamoDbEventStore implements BasicEventStore {

    private final AmazonDynamoDB amazonDynamoDB;
    private final NotificationMessagingTemplate notificationMessagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventsSchema eventsSchema = new EventsSchema();

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

    private String json(Event event) {
        try {
            return objectMapper.writeValueAsString(new TreeMap<>(event.asMap()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not write JSON", e);
        }
    }
}
