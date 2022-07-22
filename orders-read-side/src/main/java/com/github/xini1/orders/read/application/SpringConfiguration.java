package com.github.xini1.orders.read.application;

import com.github.xini1.common.Shared;
import com.github.xini1.orders.read.domain.Module;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.kafka.config.TopicBuilder;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

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
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    RpcServer rpcServer(Module module) {
        return new RpcServer(module);
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
