package com.github.xini1.orders.read.application;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.github.xini1.orders.read.view.Item;

import java.util.Map;
import java.util.UUID;

class ItemsSchema {

    private static final String TABLE_NAME = "items";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ACTIVE = "active";
    private static final String VERSION = "version";

    Binding bind(Map<String, AttributeValue> attributes) {
        return new Binding(attributes);
    }

    QueryRequest findByIdRequest(UUID id) {
        return new QueryRequest()
                .withTableName(TABLE_NAME)
                .withKeyConditionExpression(ID + " = :id")
                .withExpressionAttributeValues(Map.of(":id", new AttributeValue().withS(id.toString())));
    }

    PutItemRequest putRequest(Item item) {
        return new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(attributes(item));
    }

    ScanRequest findAllRequest() {
        return new ScanRequest()
                .withTableName(TABLE_NAME);
    }

    private Map<String, AttributeValue> attributes(Item item) {
        return Map.of(
                ID, new AttributeValue().withS(item.getId().toString()),
                NAME, new AttributeValue().withS(item.getName()),
                ACTIVE, new AttributeValue().withBOOL(item.isActive()),
                VERSION, new AttributeValue().withN(String.valueOf(item.getVersion()))
        );
    }

    static final class Binding {

        private final Map<String, AttributeValue> attributes;

        private Binding(Map<String, AttributeValue> attributes) {
            this.attributes = Map.copyOf(attributes);
        }

        Item toItem() {
            return new Item(
                    UUID.fromString(attributes.get(ID).getS()),
                    attributes.get(NAME).getS(),
                    attributes.get(ACTIVE).getBOOL(),
                    Long.parseLong(attributes.get(VERSION).getN())
            );
        }
    }
}
