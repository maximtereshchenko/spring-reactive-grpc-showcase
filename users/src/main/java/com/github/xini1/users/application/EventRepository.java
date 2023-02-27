package com.github.xini1.users.application;

import com.github.xini1.common.mongodb.EventDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
interface EventRepository extends ReactiveMongoRepository<EventDocument, UUID> {
}
