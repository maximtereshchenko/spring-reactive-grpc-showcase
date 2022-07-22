package com.github.xini1.orders.read.application;

import com.github.xini1.orders.read.port.*;
import com.github.xini1.orders.read.view.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class MongoViewStore implements ViewStore {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final OrderedItemsRepository orderedItemsRepository;
    private final TopOrderedItemRepository topOrderedItemRepository;

    MongoViewStore(
            CartRepository cartRepository,
            ItemRepository itemRepository,
            OrderedItemsRepository orderedItemsRepository,
            TopOrderedItemRepository topOrderedItemRepository
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.orderedItemsRepository = orderedItemsRepository;
        this.topOrderedItemRepository = topOrderedItemRepository;
    }

    @Override
    public Item findItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .map(ItemDocument::toItem)
                .block();
    }

    @Override
    public Cart findCart(UUID userId) {
        return cartRepository.findById(userId)
                .map(CartDocument::toCart)
                .blockOptional()
                .orElseGet(() -> new Cart(userId));
    }

    @Override
    public void save(Item item) {
        itemRepository.save(new ItemDocument(item))
                .subscribe();
    }

    @Override
    public void save(Cart cart) {
        cartRepository.save(new CartDocument(cart))
                .subscribe();
    }

    @Override
    public Iterable<Item> findAllItems() {
        return itemRepository.findAll()
                .map(ItemDocument::toItem)
                .collectList()
                .block();
    }

    @Override
    public Collection<Cart> findCartsByItemIdAndItemVersionLess(UUID itemId, long version) {
        return cartRepository.findByItemsInCart_IdAndItemsInCart_VersionLessThan(itemId, version)
                .map(CartDocument::toCart)
                .collectList()
                .block();
    }

    @Override
    public Iterable<TopOrderedItem> findAllTopOrderedItems() {
        return topOrderedItemRepository.findAll()
                .map(TopOrderedItemDocument::toTopOrderedItem)
                .collectList()
                .block();
    }

    @Override
    public TopOrderedItem findTopOrderedItem(UUID itemId) {
        return topOrderedItemRepository.findById(itemId)
                .map(TopOrderedItemDocument::toTopOrderedItem)
                .block();
    }

    @Override
    public void save(TopOrderedItem topOrderedItem) {
        topOrderedItemRepository.save(new TopOrderedItemDocument(topOrderedItem))
                .subscribe();
    }

    @Override
    public OrderedItems findOrderedItems(UUID userId) {
        return orderedItemsRepository.findById(userId)
                .map(OrderedItemsDocument::toOrderedItems)
                .blockOptional()
                .orElseGet(() -> new OrderedItems(userId));
    }

    @Override
    public void save(OrderedItems orderedItems) {
        orderedItemsRepository.save(new OrderedItemsDocument(orderedItems))
                .subscribe();
    }
}
