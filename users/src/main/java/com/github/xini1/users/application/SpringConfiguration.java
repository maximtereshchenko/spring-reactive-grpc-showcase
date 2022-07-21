package com.github.xini1.users.application;

import com.github.xini1.common.*;
import com.github.xini1.users.domain.Module;
import org.apache.kafka.clients.admin.*;
import org.springframework.boot.autoconfigure.kafka.*;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.config.*;
import org.springframework.kafka.config.*;
import reactor.kafka.sender.*;

/**
 * @author Maxim Tereshchenko
 */
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = UserRepository.class)
public class SpringConfiguration {

    @Bean
    RpcServer rpcServer(
            UserRepository userRepository,
            EventRepository eventRepository,
            KafkaProperties kafkaProperties
    ) {
        return new RpcServer(
                new Module(
                        new MongoUserStore(userRepository),
                        new MongoEventStore(
                                eventRepository,
                                KafkaSender.create(SenderOptions.create(kafkaProperties.buildProducerProperties()))
                        ),
                        new JwtProvider(),
                        new Pbkdf2HashingAlgorithm()
                )
        );
    }

    @Bean
    NewTopic eventsTopic() {
        return TopicBuilder.name(Shared.EVENTS_KAFKA_TOPIC).build();
    }
}
