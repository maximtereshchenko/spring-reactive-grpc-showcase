package com.github.xini1.common.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.github.xini1.common.event.Event;
import com.github.xini1.common.event.EventType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Maxim Tereshchenko
 */
public final class EventsSchema {

    private static final String TABLE_NAME = "events";
    private static final String AGGREGATE_ID = "aggregateId";
    private static final String VERSION = "version";
    private static final String EVENT_TYPE = "eventType";
    private static final String DATA = "data";

    private static final String AGGREGATE_ID_EXPRESSION_VALUE = ":aggregateId";
    private static final String EVENT_TYPE_EXPRESSION_VALUE = ":eventType";

    public Map<String, AttributeValue> attributes(Event event, String data) {
        return Map.of(
                AGGREGATE_ID, new AttributeValue().withS(event.aggregateId().toString()),
                VERSION, new AttributeValue().withN(String.valueOf(event.version())),
                EVENT_TYPE, new AttributeValue().withS(event.type().toString()),
                DATA, new AttributeValue().withS(data)
        );
    }

    public Binding bind(Map<String, AttributeValue> attributes) {
        return new Binding(attributes);
    }

    public PutItemRequest putRequest(Event event, String data) {
        return new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(attributes(event, data));
    }

    public ScanRequest findByAggregateIdAndEventTypeInRequest(UUID aggregateId, Collection<EventType> eventTypes) {
        return new ScanRequest()
                .withTableName(TABLE_NAME)
                .withFilterExpression(filterExpression(eventTypes.size()))
                .withExpressionAttributeValues(expressionAttributeValues(aggregateId, List.copyOf(eventTypes)));
    }

    public ScanRequest findAllRequest() {
        return new ScanRequest()
                .withTableName(TABLE_NAME);
    }

    private Map<String, AttributeValue> expressionAttributeValues(UUID aggregateId, List<EventType> eventTypes) {
        var values = new HashMap<String, AttributeValue>();
        values.put(AGGREGATE_ID_EXPRESSION_VALUE, new AttributeValue().withS(aggregateId.toString()));
        for (int i = 0; i < eventTypes.size(); i++) {
            values.put(EVENT_TYPE_EXPRESSION_VALUE + i, new AttributeValue().withS(eventTypes.get(i).toString()));
        }
        return values;
    }

    private String filterExpression(int eventTypes) {
        return String.format(
                "%s = %s and %s in %s",
                AGGREGATE_ID,
                AGGREGATE_ID_EXPRESSION_VALUE,
                EVENT_TYPE,
                IntStream.range(0, eventTypes)
                        .mapToObj(index -> EVENT_TYPE_EXPRESSION_VALUE + index)
                        .collect(Collectors.joining(", ", "(", ")"))
        );
    }

    public static final class Binding {

        private final Map<String, AttributeValue> attributes;

        private Binding(Map<String, AttributeValue> attributes) {
            this.attributes = Map.copyOf(attributes);
        }

        public EventType eventType() {
            return EventType.valueOf(attributes.get(EVENT_TYPE).getS());
        }

        public Map<String, AttributeValue> attributes() {
            return attributes;
        }

        public String data() {
            return attributes.get(DATA).getS();
        }
    }
}
