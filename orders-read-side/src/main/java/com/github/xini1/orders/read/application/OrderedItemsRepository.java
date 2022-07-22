package com.github.xini1.orders.read.application;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
interface OrderedItemsRepository extends ReactiveMongoRepository<OrderedItemsDocument, UUID> {
}
