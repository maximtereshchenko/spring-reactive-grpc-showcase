package com.github.xini1.orders.write.application;

import com.github.xini1.common.event.EventType;
import com.github.xini1.common.mongodb.EventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
interface EventRepository extends ReactiveMongoRepository<EventDocument, UUID> {

    Flux<EventDocument> findById_AggregateIdAndEventTypeIn(UUID aggregateId, Collection<EventType> eventTypes);
}
