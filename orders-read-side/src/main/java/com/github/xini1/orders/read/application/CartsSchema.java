package com.github.xini1.orders.read.application;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.github.xini1.orders.read.view.Cart;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Maxim Tereshchenko
 */
final class CartsSchema {

    private static final String TABLE_NAME = "carts";
    private static final String USER_ID = "userId";
    private static final String ITEMS_IN_CART = "itemsInCart";
    private static final String ITEM_ID_VERSION_LIST = "itemIdVersionList";
    private static final String VERSION = "version";

    private static final String ITEM_ID_VERSION_LIST_EXPRESSION_VALUE = ":itemIdVersionList";

    private final ItemsInCartSchema itemsInCartSchema = new ItemsInCartSchema();

    Binding bind(Map<String, AttributeValue> attributes) {
        return new Binding(attributes, itemsInCartSchema);
    }

    QueryRequest findByIdRequest(UUID userId) {
        return new QueryRequest()
                .withTableName(TABLE_NAME)
                .withKeyConditionExpression(USER_ID + " = :userId")
                .withExpressionAttributeValues(Map.of(":userId", new AttributeValue().withS(userId.toString())));
    }

    PutItemRequest putRequest(Cart cart) {
        return new PutItemRequest()
                .withTableName(TABLE_NAME)
                .withItem(attributes(cart));
    }

    ScanRequest findByItemIdAndItemVersionLessThanRequest(UUID itemId, long itemVersion) {
        return new ScanRequest()
                .withTableName(TABLE_NAME)
                .withFilterExpression(filterExpression(itemVersion))
                .withExpressionAttributeValues(expressionAttributeValues(itemId, itemVersion));
    }

    private Map<String, AttributeValue> expressionAttributeValues(UUID itemId, long itemVersion) {
        return LongStream.range(1, itemVersion)
                .boxed()
                .collect(
                        Collectors.toMap(
                                version -> ITEM_ID_VERSION_LIST_EXPRESSION_VALUE + version,
                                version -> new AttributeValue().withS(itemIdVersion(itemId, version))
                        )
                );
    }

    private String filterExpression(long itemVersion) {
        return LongStream.range(1, itemVersion)
                .mapToObj(version ->
                        String.format(
                                "contains(%s,%s)",
                                ITEM_ID_VERSION_LIST,
                                ITEM_ID_VERSION_LIST_EXPRESSION_VALUE + version
                        )
                )
                .collect(Collectors.joining(" or "));
    }

    private Map<String, AttributeValue> attributes(Cart cart) {
        var attributes = new HashMap<String, AttributeValue>();
        attributes.put(USER_ID, new AttributeValue().withS(cart.getUserId().toString()));
        attributes.put(ITEMS_IN_CART, new AttributeValue().withL(itemsInCartValues(cart)));
        if (!cart.getItemsInCart().isEmpty()) {
            attributes.put(ITEM_ID_VERSION_LIST, new AttributeValue().withSS(itemIdVersionValues(cart)));
        }
        attributes.put(VERSION, new AttributeValue().withN(String.valueOf(cart.getVersion())));
        return attributes;
    }

    private Collection<String> itemIdVersionValues(Cart cart) {
        return cart.getItemsInCart()
                .stream()
                .map(itemInCart -> itemIdVersion(itemInCart.getId(), itemInCart.getVersion()))
                .collect(Collectors.toSet());
    }

    private String itemIdVersion(UUID itemId, long itemVersion) {
        return itemId.toString() + '#' + itemVersion;
    }

    private List<AttributeValue> itemsInCartValues(Cart cart) {
        return cart.getItemsInCart()
                .stream()
                .map(itemsInCartSchema::attributes)
                .map(attributes -> new AttributeValue().withM(attributes))
                .collect(Collectors.toList());
    }

    static final class Binding {

        private final Map<String, AttributeValue> attributes;
        private final ItemsInCartSchema itemsInCartSchema;

        private Binding(Map<String, AttributeValue> attributes, ItemsInCartSchema itemsInCartSchema) {
            this.attributes = Map.copyOf(attributes);
            this.itemsInCartSchema = itemsInCartSchema;
        }

        Cart toCart() {
            return new Cart(
                    UUID.fromString(attributes.get(USER_ID).getS()),
                    attributes.get(ITEMS_IN_CART)
                            .getL()
                            .stream()
                            .map(AttributeValue::getM)
                            .map(itemsInCartSchema::bind)
                            .map(ItemsInCartSchema.Binding::toItemInCart)
                            .collect(Collectors.toList()),
                    Long.parseLong(attributes.get(VERSION).getN())
            );
        }
    }

    private static class ItemsInCartSchema {

        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String ACTIVE = "active";
        private static final String QUANTITY = "quantity";
        private static final String VERSION = "version";

        ItemsInCartSchema.Binding bind(Map<String, AttributeValue> attributes) {
            return new ItemsInCartSchema.Binding(attributes);
        }

        Map<String, AttributeValue> attributes(Cart.ItemInCart itemInCart) {
            return Map.of(
                    ID, new AttributeValue().withS(itemInCart.getId().toString()),
                    NAME, new AttributeValue().withS(itemInCart.getName()),
                    ACTIVE, new AttributeValue().withBOOL(Boolean.valueOf(itemInCart.isActive())),
                    QUANTITY, new AttributeValue().withN(String.valueOf(itemInCart.getQuantity())),
                    VERSION, new AttributeValue().withN(String.valueOf(itemInCart.getVersion()))
            );
        }

        private static final class Binding {

            private final Map<String, AttributeValue> attributes;

            private Binding(Map<String, AttributeValue> attributes) {
                this.attributes = Map.copyOf(attributes);
            }

            Cart.ItemInCart toItemInCart() {
                return new Cart.ItemInCart(
                        UUID.fromString(attributes.get(ID).getS()),
                        attributes.get(NAME).getS(),
                        attributes.get(ACTIVE).getBOOL(),
                        Integer.parseInt(attributes.get(QUANTITY).getN()),
                        Long.parseLong(attributes.get(VERSION).getN())
                );
            }
        }
    }
}
