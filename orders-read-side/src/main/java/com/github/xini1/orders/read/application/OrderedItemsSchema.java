package com.github.xini1.orders.read.application;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.github.xini1.orders.read.view.OrderedItems;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

class OrderedItemsSchema {

    private static final String TABLE_NAME = "orderedItems";
    private static final String USER_ID = "userId";
    private static final String ORDERS = "orders";

    private final OrdersSchema ordersSchema = new OrdersSchema();

    Binding bind(Map<String, AttributeValue> attributes) {
        return new Binding(attributes, ordersSchema);
    }

    QueryRequest findByUserIdRequest(UUID userId) {
        return new QueryRequest()
                .withTableName(TABLE_NAME)
                .withKeyConditionExpression(USER_ID + " = :userId")
                .withExpressionAttributeValues(Map.of(":userId", new AttributeValue().withS(userId.toString())));
    }

    PutItemRequest putRequest(OrderedItems orderedItems) {
        return new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(attributes(orderedItems));
    }

    private Map<String, AttributeValue> attributes(OrderedItems orderedItems) {
        return Map.of(
                USER_ID,
                new AttributeValue().withS(orderedItems.getUserId().toString()),
                ORDERS,
                new AttributeValue()
                        .withL(
                                orderedItems.getOrders()
                                        .stream()
                                        .map(ordersSchema::attributes)
                                        .map(attributes -> new AttributeValue().withM(attributes))
                                        .collect(Collectors.toList())
                        )
        );
    }

    static final class Binding {

        private final Map<String, AttributeValue> attributes;
        private final OrdersSchema ordersSchema;

        private Binding(Map<String, AttributeValue> attributes, OrdersSchema ordersSchema) {
            this.attributes = Map.copyOf(attributes);
            this.ordersSchema = ordersSchema;
        }

        OrderedItems toOrderedItems() {
            return new OrderedItems(
                    UUID.fromString(attributes.get(USER_ID).getS()),
                    attributes.get(ORDERS)
                            .getL()
                            .stream()
                            .map(AttributeValue::getM)
                            .map(ordersSchema::bind)
                            .map(OrdersSchema.Binding::toOrder)
                            .collect(Collectors.toList())
            );
        }
    }

    private static final class OrdersSchema {

        private static final String TIMESTAMP = "timestamp";
        private static final String ITEMS = "items";
        private final ItemsInOrderSchema itemsInOrderSchema = new ItemsInOrderSchema();

        Binding bind(Map<String, AttributeValue> attributes) {
            return new Binding(attributes, itemsInOrderSchema);
        }

        Map<String, AttributeValue> attributes(OrderedItems.Order order) {
            return Map.of(
                    TIMESTAMP,
                    new AttributeValue().withS(order.getTimestamp().toString()),
                    ITEMS,
                    new AttributeValue()
                            .withL(
                                    order.getItems()
                                            .stream()
                                            .map(itemsInOrderSchema::attributes)
                                            .map(attributes -> new AttributeValue().withM(attributes))
                                            .collect(Collectors.toList())
                            )
            );
        }

        static final class Binding {

            private final Map<String, AttributeValue> attributes;
            private final ItemsInOrderSchema itemsInOrderSchema;

            private Binding(Map<String, AttributeValue> attributes, ItemsInOrderSchema itemsInOrderSchema) {
                this.attributes = Map.copyOf(attributes);
                this.itemsInOrderSchema = itemsInOrderSchema;
            }

            OrderedItems.Order toOrder() {
                return new OrderedItems.Order(
                        Instant.parse(attributes.get(TIMESTAMP).getS()),
                        attributes.get(ITEMS)
                                .getL()
                                .stream()
                                .map(AttributeValue::getM)
                                .map(itemsInOrderSchema::bind)
                                .map(ItemsInOrderSchema.Binding::toItemInOrder)
                                .collect(Collectors.toList())
                );
            }
        }

        private static final class ItemsInOrderSchema {

            private static final String ID = "id";
            private static final String QUANTITY = "quantity";

            Binding bind(Map<String, AttributeValue> attributes) {
                return new Binding(attributes);
            }

            Map<String, AttributeValue> attributes(OrderedItems.ItemInOrder itemInOrder) {
                return Map.of(
                        ID, new AttributeValue().withS(itemInOrder.getId().toString()),
                        QUANTITY, new AttributeValue().withN(String.valueOf(itemInOrder.getQuantity()))
                );
            }

            static final class Binding {

                private final Map<String, AttributeValue> attributes;

                private Binding(Map<String, AttributeValue> attributes) {
                    this.attributes = Map.copyOf(attributes);
                }

                OrderedItems.ItemInOrder toItemInOrder() {
                    return new OrderedItems.ItemInOrder(
                            UUID.fromString(attributes.get(ID).getS()),
                            Integer.parseInt(attributes.get(QUANTITY).getN())
                    );
                }
            }
        }
    }
}
