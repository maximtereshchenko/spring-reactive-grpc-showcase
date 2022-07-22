package com.github.xini1.orders.read.application;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
interface ItemRepository extends ReactiveMongoRepository<ItemDocument, UUID> {
}
