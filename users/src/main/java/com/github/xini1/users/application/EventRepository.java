package com.github.xini1.users.application;

import com.github.xini1.common.mongodb.*;
import org.springframework.data.mongodb.repository.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
interface EventRepository extends ReactiveMongoRepository<EventDocument, UUID> {
}
