package com.github.xini1.users.application;

import com.github.xini1.users.domain.Module;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.config.*;

/**
 * @author Maxim Tereshchenko
 */
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = UserRepository.class)
public class ApplicationConfiguration {

    @Bean
    RpcServer rpcServer(UserRepository userRepository, EventRepository eventRepository) {
        return new RpcServer(
                new Module(
                        new MongoUserStore(userRepository),
                        new MongoEventStore(eventRepository),
                        new JwtProvider(),
                        new Pbkdf2HashingAlgorithm()
                )
        );
    }
}
