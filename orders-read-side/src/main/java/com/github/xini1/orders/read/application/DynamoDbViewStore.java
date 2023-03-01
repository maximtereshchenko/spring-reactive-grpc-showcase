package com.github.xini1.orders.read.application;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.github.xini1.orders.read.port.ViewStore;
import com.github.xini1.orders.read.view.Cart;
import com.github.xini1.orders.read.view.Item;
import com.github.xini1.orders.read.view.OrderedItems;
import com.github.xini1.orders.read.view.TopOrderedItem;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Maxim Tereshchenko
 */
final class DynamoDbViewStore implements ViewStore {

    private final AmazonDynamoDB amazonDynamoDB;
    private final CartsSchema cartsSchema = new CartsSchema();
    private final ItemsSchema itemsSchema = new ItemsSchema();
    private final TopOrderedItemsSchema topOrderedItemsSchema = new TopOrderedItemsSchema();
    private final OrderedItemsSchema orderedItemsSchema = new OrderedItemsSchema();

    DynamoDbViewStore(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public Item findItem(UUID itemId) {
        return itemsSchema.bind(amazonDynamoDB.query(itemsSchema.findByIdRequest(itemId)).getItems().get(0)).toItem();
    }

    @Override
    public Cart findCart(UUID userId) {
        var result = amazonDynamoDB.query(cartsSchema.findByIdRequest(userId));
        if (result.getCount() == 0) {
            return new Cart(userId);
        }
        return cartsSchema.bind(result.getItems().get(0)).toCart();
    }

    @Override
    public void save(Item item) {
        amazonDynamoDB.putItem(itemsSchema.putRequest(item));
    }

    @Override
    public void save(Cart cart) {
        amazonDynamoDB.putItem(cartsSchema.putRequest(cart));
    }

    @Override
    public Iterable<Item> findAllItems() {
        return amazonDynamoDB.scan(itemsSchema.findAllRequest())
                .getItems()
                .stream()
                .map(itemsSchema::bind)
                .map(ItemsSchema.Binding::toItem)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Cart> findCartsByItemIdAndItemVersionLess(UUID itemId, long version) {
        return amazonDynamoDB.scan(cartsSchema.findByItemIdAndItemVersionLessThanRequest(itemId, version))
                .getItems()
                .stream()
                .map(cartsSchema::bind)
                .map(CartsSchema.Binding::toCart)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<TopOrderedItem> findAllTopOrderedItems() {
        return amazonDynamoDB.scan(topOrderedItemsSchema.findAllRequest())
                .getItems()
                .stream()
                .map(topOrderedItemsSchema::bind)
                .map(TopOrderedItemsSchema.Binding::toTopOrderedItem)
                .collect(Collectors.toList());
    }

    @Override
    public TopOrderedItem findTopOrderedItem(UUID itemId) {
        return topOrderedItemsSchema.bind(
                        amazonDynamoDB.query(topOrderedItemsSchema.findByIdRequest(itemId)).getItems().get(0)
                )
                .toTopOrderedItem();
    }

    @Override
    public void save(TopOrderedItem topOrderedItem) {
        amazonDynamoDB.putItem(topOrderedItemsSchema.putRequest(topOrderedItem));
    }

    @Override
    public OrderedItems findOrderedItems(UUID userId) {
        var result = amazonDynamoDB.query(orderedItemsSchema.findByUserIdRequest(userId));
        if (result.getCount() == 0) {
            return new OrderedItems(userId);
        }
        return orderedItemsSchema.bind(result.getItems().get(0)).toOrderedItems();
    }

    @Override
    public void save(OrderedItems orderedItems) {
        amazonDynamoDB.putItem(orderedItemsSchema.putRequest(orderedItems));
    }
}
