package com.github.xini1.orders.write.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.xini1.common.Shared;
import com.github.xini1.common.dynamodb.EventsSchema;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemActivated;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.orders.write.Main;
import com.github.xini1.orders.write.rpc.*;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthGrpc;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.github.xini1.Await.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Maxim Tereshchenko
 */
@SpringBootTest(classes = {Main.class, IntegrationTest.TestConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
final class IntegrationTest {

    @Container
    private static final LocalStackContainer LOCAL_STACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:1.4")
    )
            .withServices(
                    LocalStackContainer.Service.SNS,
                    LocalStackContainer.Service.SQS,
                    LocalStackContainer.Service.DYNAMODB
            )
            .withCopyFileToContainer(
                    MountableFile.forHostPath("../localstack-setup.sh"),
                    "/etc/localstack/init/ready.d/localstack-setup.sh"
            )
            .waitingFor(Wait.forHealthcheck());

    private static final String USER_ID = "00000000-0000-0000-0000-000000000001";

    static {
        LOCAL_STACK.start();
        System.setProperty("aws.accessKeyId", LOCAL_STACK.getAccessKey());
        System.setProperty("aws.secretKey", LOCAL_STACK.getSecretKey());
    }

    private final ManagedChannel channel = Grpc.newChannelBuilderForAddress(
                    "localhost",
                    8080,
                    TlsChannelCredentials.newBuilder()
                            .trustManager(Shared.rootCertificate())
                            .build()
            )
            .build();
    private final OrderWriteServiceGrpc.OrderWriteServiceBlockingStub stub =
            OrderWriteServiceGrpc.newBlockingStub(channel);
    private final HealthGrpc.HealthBlockingStub healthStub = HealthGrpc.newBlockingStub(channel);
    private final EventsSchema eventsSchema = new EventsSchema();
    private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            LOCAL_STACK.getEndpointOverride(Service.DYNAMODB).toString()
                            , LOCAL_STACK.getRegion()
                    )
            )
            .build();
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    private String itemId;

    IntegrationTest() throws IOException {}

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("cloud.aws.region.static", LOCAL_STACK::getRegion);
        registry.add(
                "application.sns.endpoint",
                () -> LOCAL_STACK.getEndpointOverride(Service.SNS).toString()
        );
        registry.add(
                "application.dynamodb.endpoint",
                () -> LOCAL_STACK.getEndpointOverride(Service.DYNAMODB).toString()
        );
    }

    @Test
    @Order(0)
    void adminCanCreateItem() {
        itemId = stub.create(
                        CreateItemRequest.newBuilder()
                                .setUserId(USER_ID)
                                .setUserType("ADMIN")
                                .setName("item")
                                .build()
                )
                .getItemId();

        assertThat(itemId).isNotNull();
        assertThat(amazonDynamoDB.scan(eventsSchema.findAllRequest()).getItems())
                .containsExactly(itemCreatedEvent());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(itemCreatedEventJson())
        );
    }

    @Test
    @Order(1)
    void adminCanDeactivateItem() {
        stub.deactivate(
                DeactivateItemRequest.newBuilder()
                        .setUserId(USER_ID)
                        .setUserType("ADMIN")
                        .setItemId(itemId)
                        .build()
        );

        assertThat(amazonDynamoDB.scan(eventsSchema.findAllRequest()).getItems())
                .hasSize(2)
                .contains(itemDeactivatedEvent());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(itemDeactivatedEventJson())
        );
    }

    @Test
    @Order(2)
    void adminCanActivateItem() {
        stub.activate(
                ActivateItemRequest.newBuilder()
                        .setUserId(USER_ID)
                        .setUserType("ADMIN")
                        .setItemId(itemId)
                        .build()
        );

        assertThat(amazonDynamoDB.scan(eventsSchema.findAllRequest()).getItems())
                .hasSize(3)
                .contains(itemActivatedEvent());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(itemActivatedEventJson())
        );
    }

    @Test
    @Order(3)
    void userCanAddItemToCart() {
        stub.add(
                AddItemToCartRequest.newBuilder()
                        .setUserId(USER_ID)
                        .setUserType("REGULAR")
                        .setItemId(itemId)
                        .setQuantity(2)
                        .build()
        );


        await(() -> {
                    assertThat(amazonDynamoDB.scan(eventsSchema.findAllRequest()).getItems())
                            .hasSize(4)
                            .contains(itemAddedToCartEvent());
                    assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                            .isEqualTo(itemAddedToCartEventJson());
                }
        );
    }

    @Test
    @Order(4)
    void userCanRemoveItemFromCart() {
        stub.remove(
                RemoveItemFromCartRequest.newBuilder()
                        .setUserId(USER_ID)
                        .setUserType("REGULAR")
                        .setItemId(itemId)
                        .setQuantity(1)
                        .build()
        );

        await(() -> {
                    assertThat(amazonDynamoDB.scan(eventsSchema.findAllRequest()).getItems())
                            .hasSize(5)
                            .contains(itemRemovedFromCartEvent());
                    assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                            .isEqualTo(itemRemovedFromCartEventJson());
                }
        );
    }

    @Test
    @Order(5)
    void userCanOrderItemsInCart() {
        stub.order(
                OrderItemsInCartRequest.newBuilder()
                        .setUserId(USER_ID)
                        .setUserType("REGULAR")
                        .build()
        );

        await(() -> {
                    assertThat(amazonDynamoDB.scan(eventsSchema.findAllRequest()).getItems())
                            .hasSize(6)
                            .contains(itemsOrderedEvent());
                    assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                            .isEqualTo(itemsOrderedEventJson());
                }
        );
    }

    @Test
    void canPerformHealthCheck() {
        assertThat(healthStub.check(HealthCheckRequest.newBuilder().build()).getStatusValue()).isOne();
    }

    private Map<String, AttributeValue> itemCreatedEvent() {
        return eventsSchema.attributes(
                new ItemCreated(UUID.fromString(itemId), UUID.fromString(USER_ID), "item", 1),
                itemCreatedEventJson()
        );
    }

    private Map<String, AttributeValue> itemDeactivatedEvent() {
        return eventsSchema.attributes(
                new ItemDeactivated(UUID.fromString(itemId), UUID.fromString(USER_ID), 2),
                itemDeactivatedEventJson()
        );
    }

    private Map<String, AttributeValue> itemActivatedEvent() {
        return eventsSchema.attributes(
                new ItemActivated(UUID.fromString(itemId), UUID.fromString(USER_ID), 3),
                itemActivatedEventJson()
        );
    }

    private Map<String, AttributeValue> itemAddedToCartEvent() {
        return eventsSchema.attributes(
                new ItemAddedToCart(UUID.fromString(USER_ID), UUID.fromString(itemId), 2, 1),
                itemAddedToCartEventJson()
        );
    }

    private Map<String, AttributeValue> itemRemovedFromCartEvent() {
        return eventsSchema.attributes(
                new ItemRemovedFromCart(UUID.fromString(USER_ID), UUID.fromString(itemId), 1, 2),
                itemRemovedFromCartEventJson()
        );
    }

    private Map<String, AttributeValue> itemsOrderedEvent() {
        return eventsSchema.attributes(
                new ItemsOrdered(UUID.fromString(USER_ID), 3),
                itemsOrderedEventJson()
        );
    }

    private String itemCreatedEventJson() {
        return "{\"eventType\":\"ITEM_CREATED\",\"itemId\":\"" + itemId + "\",\"name\":\"item\",\"userId\":\"" +
                USER_ID + "\",\"version\":\"1\"}";
    }

    private String itemDeactivatedEventJson() {
        return "{\"eventType\":\"ITEM_DEACTIVATED\",\"itemId\":\"" + itemId + "\",\"userId\":\"" + USER_ID +
                "\",\"version\":\"2\"}";
    }

    private String itemActivatedEventJson() {
        return "{\"eventType\":\"ITEM_ACTIVATED\",\"itemId\":\"" + itemId + "\",\"userId\":\"" + USER_ID +
                "\",\"version\":\"3\"}";
    }

    private String itemAddedToCartEventJson() {
        return "{\"eventType\":\"ITEM_ADDED_TO_CART\",\"itemId\":\"" + itemId + "\",\"quantity\":\"2\",\"userId\":\"" +
                USER_ID + "\",\"version\":\"1\"}";
    }

    private String itemRemovedFromCartEventJson() {
        return "{\"eventType\":\"ITEM_REMOVED_FROM_CART\",\"itemId\":\"" + itemId +
                "\",\"quantity\":\"1\",\"userId\":\"" + USER_ID + "\",\"version\":\"2\"}";
    }

    private String itemsOrderedEventJson() {
        return "{\"eventType\":\"ITEMS_ORDERED\",\"userId\":\"" + USER_ID + "\",\"version\":\"3\"}";
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public QueueMessagingTemplate queueMessagingTemplate(ResourceIdResolver resourceIdResolver) {
            return new QueueMessagingTemplate(
                    AmazonSQSAsyncClientBuilder.standard()
                            .withEndpointConfiguration(
                                    new AwsClientBuilder.EndpointConfiguration(
                                            LOCAL_STACK.getEndpointOverride(Service.SNS).toString(),
                                            LOCAL_STACK.getRegion()
                                    )
                            )
                            .build(),
                    resourceIdResolver,
                    new StringMessageConverter()
            );
        }
    }
}
