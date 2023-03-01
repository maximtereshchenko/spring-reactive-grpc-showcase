package com.github.xini1.orders.read.application;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.github.xini1.orders.read.view.TopOrderedItem;

import java.util.Map;
import java.util.UUID;

class TopOrderedItemsSchema {

    private static final String TABLE_NAME = "topOrderedItems";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TIMES_ORDERED = "timesOrdered";

    Binding bind(Map<String, AttributeValue> attributes) {
        return new Binding(attributes);
    }

    QueryRequest findByIdRequest(UUID id) {
        return new QueryRequest()
                .withTableName(TABLE_NAME)
                .withKeyConditionExpression(ID + " = :id")
                .withExpressionAttributeValues(Map.of(":id", new AttributeValue().withS(id.toString())));
    }

    ScanRequest findAllRequest() {
        return new ScanRequest()
                .withTableName(TABLE_NAME);
    }

    PutItemRequest putRequest(TopOrderedItem topOrderedItem) {
        return new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(attributes(topOrderedItem));
    }

    private Map<String, AttributeValue> attributes(TopOrderedItem topOrderedItem) {
        return Map.of(
                ID, new AttributeValue().withS(topOrderedItem.getId().toString()),
                NAME, new AttributeValue().withS(topOrderedItem.getName()),
                TIMES_ORDERED, new AttributeValue().withN(String.valueOf(topOrderedItem.getTimesOrdered()))
        );
    }

    static final class Binding {

        private final Map<String, AttributeValue> attributes;

        private Binding(Map<String, AttributeValue> attributes) {
            this.attributes = Map.copyOf(attributes);
        }

        TopOrderedItem toTopOrderedItem() {
            return new TopOrderedItem(
                    UUID.fromString(attributes.get(ID).getS()),
                    attributes.get(NAME).getS(),
                    Long.parseLong(attributes.get(TIMES_ORDERED).getN())
            );
        }
    }
}
