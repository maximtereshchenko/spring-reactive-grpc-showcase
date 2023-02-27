package com.github.xini1.orders.read.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.xini1.common.rpc.RpcServer;
import com.github.xini1.orders.read.domain.Module;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
    public AmazonSQS amazonSQS(
            @Value("${application.sqs.endpoint}") String sqsEndpoint,
            @Value("${cloud.aws.region.static}") String awsRegion,
            ResourceIdResolver resourceIdResolver
    ) {
        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(sqsEndpoint, awsRegion))
                .build();
    }

    @Bean
    SqsEventConsumer sqsEventConsumer(Module module) {
        return new SqsEventConsumer(module);
    }
}
