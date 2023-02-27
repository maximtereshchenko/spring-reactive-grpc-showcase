package com.github.xini1.users.application;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
interface UserRepository extends ReactiveMongoRepository<UserDocument, UUID> {

    Mono<UserDocument> findByUsername(String username);
}
