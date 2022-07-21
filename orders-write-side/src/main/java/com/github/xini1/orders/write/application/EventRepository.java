package com.github.xini1.orders.write.application;

import com.github.xini1.common.event.*;
import com.github.xini1.common.mongodb.*;
import org.springframework.data.mongodb.repository.*;
import reactor.core.publisher.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
interface EventRepository extends ReactiveMongoRepository<EventDocument, UUID> {

    Flux<EventDocument> findById_AggregateIdAndEventTypeIn(UUID aggregateId, Collection<EventType> eventTypes);
}
