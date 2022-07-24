package com.github.xini1.orders.read.application;

import com.github.xini1.common.*;
import com.github.xini1.common.rpc.*;
import com.github.xini1.orders.read.domain.Module;
import org.apache.kafka.clients.admin.*;
import org.springframework.boot.autoconfigure.kafka.*;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.repository.config.*;
import org.springframework.kafka.config.*;
import reactor.kafka.receiver.*;

import java.time.*;
import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = CartRepository.class)
public class SpringConfiguration {

    @Bean
    Module module(
            CartRepository cartRepository,
            ItemRepository itemRepository,
            OrderedItemsRepository orderedItemsRepository,
            TopOrderedItemRepository topOrderedItemRepository,
            Clock clock
    ) {
        return new Module(
                new MongoViewStore(
                        cartRepository,
                        itemRepository,
                        orderedItemsRepository,
                        topOrderedItemRepository
                ),
                clock
        );
    }

    @Bean
    @Profile("!test")
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @Profile("test")
    Clock fixed() {
        return Clock.fixed(
                LocalDateTime.of(2020, 1, 1, 1, 0)
                        .toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );
    }

    @Bean
    RpcServer rpcServer(Module module) {
        return new RpcServer(
                new OrderRpcService(
                        module.viewCartUseCase(),
                        module.viewItemsUseCase(),
                        module.viewOrderedItemsUseCase(),
                        module.viewTopOrderedItemsUseCase()
                )
        );
    }

    @Bean
    KafkaEventConsumer kafkaEventConsumer(KafkaProperties kafkaProperties, Module module) {
        return new KafkaEventConsumer(
                KafkaReceiver.create(
                        ReceiverOptions.<UUID, String>create(kafkaProperties.buildConsumerProperties())
                                .subscription(List.of(Shared.EVENTS_KAFKA_TOPIC))
                ),
                module
        );
    }

    @Bean
    NewTopic eventsTopic() {
        return TopicBuilder.name(Shared.EVENTS_KAFKA_TOPIC).build();
    }
}
