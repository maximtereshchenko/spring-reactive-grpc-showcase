package com.github.xini1.users.application;

import org.springframework.data.mongodb.repository.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
interface EventRepository extends ReactiveMongoRepository<EventDocument, UUID> {
}
