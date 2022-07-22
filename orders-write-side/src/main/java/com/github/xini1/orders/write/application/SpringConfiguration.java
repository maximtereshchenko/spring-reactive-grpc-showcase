package com.github.xini1.orders.write.application;

import com.github.xini1.common.*;
import com.github.xini1.common.rpc.*;
import com.github.xini1.orders.write.domain.Module;
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
@EnableReactiveMongoRepositories(basePackageClasses = EventRepository.class)
public class SpringConfiguration {

    @Bean
    RpcServer rpcServer(
            EventRepository eventRepository,
            KafkaProperties kafkaProperties
    ) {
        var module = new Module(
                new MongoEventStore(
                        eventRepository,
                        KafkaSender.create(SenderOptions.create(kafkaProperties.buildProducerProperties()))
                )
        );
        return new RpcServer(
                new OrderRpcService(
                        module.activateItemUseCase(),
                        module.addItemToCartUseCase(),
                        module.createItemUseCase(),
                        module.deactivateItemUseCase(),
                        module.orderItemsInCartUseCase(),
                        module.removeItemFromCartUseCase()
                )
        );
    }

    @Bean
    NewTopic eventsTopic() {
        return TopicBuilder.name(Shared.EVENTS_KAFKA_TOPIC).build();
    }
}
