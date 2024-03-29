package com.github.xini1.orders.read.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.xini1.common.rpc.RpcServer;
import com.github.xini1.orders.read.domain.Module;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Maxim Tereshchenko
 */
@Configuration
public class SpringConfiguration {

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
    Module module(
            @Value("${application.dynamodb.endpoint}") String dynamodbEndpoint,
            @Value("${cloud.aws.region.static}") String awsRegion,
            Clock clock
    ) {
        return new Module(
                new DynamoDbViewStore(
                        AmazonDynamoDBClientBuilder.standard()
                                .withEndpointConfiguration(
                                        new AwsClientBuilder.EndpointConfiguration(dynamodbEndpoint, awsRegion)
                                )
                                .build()
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
    RpcServer rpcServer(Module module) throws IOException {
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
    SqsEventConsumer sqsEventConsumer(Module module) {
        return new SqsEventConsumer(module);
    }
}
