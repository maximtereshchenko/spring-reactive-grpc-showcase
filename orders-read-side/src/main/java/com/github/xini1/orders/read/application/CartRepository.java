package com.github.xini1.orders.read.application;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
interface CartRepository extends ReactiveMongoRepository<CartDocument, UUID> {

    Flux<CartDocument> findByItemsInCart_IdAndItemsInCart_VersionLessThan(UUID itemId, long version);
}
