package com.github.xini1.users.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.github.xini1.common.rpc.RpcServer;
import com.github.xini1.users.domain.Module;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.StringMessageConverter;

/**
 * @author Maxim Tereshchenko
 */
@Configuration
public class SpringConfiguration {

    @Bean
    RpcServer rpcServer(
            @Value("${application.sns.endpoint}") String snsEndpoint,
            @Value("${application.dynamodb.endpoint}") String dynamodbEndpoint,
            @Value("${cloud.aws.region.static}") String awsRegion,
            ResourceIdResolver resourceIdResolver
    ) {
        var amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(dynamodbEndpoint, awsRegion)
                )
                .build();
        var module = new Module(
                new DynamoDbUserStore(amazonDynamoDB),
                new DynamoDbEventStore(
                        amazonDynamoDB,
                        new NotificationMessagingTemplate(
                                AmazonSNSClientBuilder.standard()
                                        .withEndpointConfiguration(
                                                new AwsClientBuilder.EndpointConfiguration(snsEndpoint, awsRegion)
                                        )
                                        .build(),
                                resourceIdResolver,
                                new StringMessageConverter()
                        )
                ),
                new JwtProvider(),
                new Pbkdf2HashingAlgorithm()
        );
        return new RpcServer(
                new UserRpcService(module.registerUseCase(), module.loginUseCase(), module.decodeJwtUseCase())
        );
    }
}
