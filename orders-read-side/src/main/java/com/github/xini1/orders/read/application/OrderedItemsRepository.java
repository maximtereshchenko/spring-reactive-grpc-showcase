package com.github.xini1.orders.read.application;

import org.springframework.data.mongodb.repository.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
interface OrderedItemsRepository extends ReactiveMongoRepository<OrderedItemsDocument, UUID> {
}
