package com.github.xini1.users.application;

import org.springframework.data.mongodb.repository.*;
import reactor.core.publisher.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
interface UserRepository extends ReactiveMongoRepository<UserDocument, UUID> {

    Mono<UserDocument> findByUsername(String username);
}
