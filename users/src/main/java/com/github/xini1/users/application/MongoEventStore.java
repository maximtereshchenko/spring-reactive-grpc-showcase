package com.github.xini1.users.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.BasicEventStore;
import com.github.xini1.common.event.Event;
import com.github.xini1.common.mongodb.EventDocument;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;

import java.util.TreeMap;

/**
 * @author Maxim Tereshchenko
 */
final class MongoEventStore implements BasicEventStore {

    private final EventRepository eventRepository;
    private final NotificationMessagingTemplate notificationMessagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    MongoEventStore(
            EventRepository eventRepository,
            NotificationMessagingTemplate notificationMessagingTemplate
    ) {
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

    private String json(Event event) {
        try {
            return objectMapper.writeValueAsString(new TreeMap<>(event.asMap()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not write JSON", e);
        }
    }
}
