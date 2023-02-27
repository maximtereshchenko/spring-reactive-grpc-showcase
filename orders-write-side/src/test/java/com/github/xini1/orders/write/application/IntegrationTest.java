package com.github.xini1.orders.write.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.EventType;
import com.github.xini1.common.mongodb.EventDocument;
import com.github.xini1.orders.write.Main;
import com.github.xini1.orders.write.rpc.*;
import io.grpc.ManagedChannelBuilder;
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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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
    private static final MongoDBContainer MONGO_DB = new MongoDBContainer(
            DockerImageName.parse("mongo:5.0.9")
    );
    @Container
    private static final LocalStackContainer LOCAL_STACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:1.4")
    )
            .withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS)
            .withFileSystemBind(
                    "../localstack-setup.sh",
                    "/etc/localstack/init/ready.d/localstack-setup.sh"
            );

    static {
        MONGO_DB.start();
        LOCAL_STACK.start();
        System.setProperty("aws.accessKeyId", LOCAL_STACK.getAccessKey());
        System.setProperty("aws.secretKey", LOCAL_STACK.getSecretKey());
    }

    private final OrderWriteServiceGrpc.OrderWriteServiceBlockingStub stub = OrderWriteServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build()
    );
    private final String userId = "00000000-0000-0000-0000-000000000001";
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    private String itemId;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB::getReplicaSetUrl);
        registry.add("cloud.aws.region.static", LOCAL_STACK::getRegion);
        registry.add(
                "application.sns.endpoint",
                () -> LOCAL_STACK.getEndpointOverride(LocalStackContainer.Service.SNS).toString()
        );
    }

    @Test
    @Order(0)
    void adminCanCreateItem() {
        itemId = stub.create(
                        CreateItemRequest.newBuilder()
                                .setUserId(userId)
                                .setUserType("ADMIN")
                                .setName("item")
                                .build()
                )
                .getItemId();

        assertThat(itemId).isNotNull();
        assertThat(eventRepository.findAll().collectList().block())
                .hasSize(1)
                .first()
                .isEqualTo(itemCreatedEventDocument());
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
                        .setUserId(userId)
                        .setUserType("ADMIN")
                        .setItemId(itemId)
                        .build()
        );

        assertThat(eventRepository.findAll().collectList().block())
                .hasSize(2)
                .last()
                .isEqualTo(itemDeactivatedEventDocument());
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
                        .setUserId(userId)
                        .setUserType("ADMIN")
                        .setItemId(itemId)
                        .build()
        );

        assertThat(eventRepository.findAll().collectList().block())
                .hasSize(3)
                .last()
                .isEqualTo(itemActivatedEventDocument());
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
                        .setUserId(userId)
                        .setUserType("REGULAR")
                        .setItemId(itemId)
                        .setQuantity(2)
                        .build()
        );

        assertThat(eventRepository.findAll().collectList().block())
                .hasSize(4)
                .last()
                .isEqualTo(itemAddedToCartEventDocument());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(itemAddedToCartEventJson())
        );
    }

    @Test
    @Order(4)
    void userCanRemoveItemFromCart() {
        stub.remove(
                RemoveItemFromCartRequest.newBuilder()
                        .setUserId(userId)
                        .setUserType("REGULAR")
                        .setItemId(itemId)
                        .setQuantity(1)
                        .build()
        );

        assertThat(eventRepository.findAll().collectList().block())
                .hasSize(5)
                .last()
                .isEqualTo(itemRemovedFromCartEventDocument());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(itemRemovedFromCartEventJson())
        );
    }

    @Test
    @Order(5)
    void userCanOrderItemsInCart() {
        stub.order(
                OrderItemsInCartRequest.newBuilder()
                        .setUserId(userId)
                        .setUserType("REGULAR")
                        .build()
        );

        assertThat(eventRepository.findAll().collectList().block())
                .hasSize(6)
                .last()
                .isEqualTo(itemsOrderedEventDocument());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(itemsOrderedEventJson())
        );
    }

    private EventDocument itemCreatedEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.ITEM_CREATED);
        eventDocument.setAggregateId(UUID.fromString(itemId));
        eventDocument.setVersion(1);
        eventDocument.setData(itemCreatedEventJson());
        return eventDocument;
    }

    private EventDocument itemDeactivatedEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.ITEM_DEACTIVATED);
        eventDocument.setAggregateId(UUID.fromString(itemId));
        eventDocument.setVersion(2);
        eventDocument.setData(itemDeactivatedEventJson());
        return eventDocument;
    }

    private EventDocument itemActivatedEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.ITEM_ACTIVATED);
        eventDocument.setAggregateId(UUID.fromString(itemId));
        eventDocument.setVersion(3);
        eventDocument.setData(itemActivatedEventJson());
        return eventDocument;
    }

    private EventDocument itemAddedToCartEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.ITEM_ADDED_TO_CART);
        eventDocument.setAggregateId(UUID.fromString(userId));
        eventDocument.setVersion(1);
        eventDocument.setData(itemAddedToCartEventJson());
        return eventDocument;
    }

    private EventDocument itemRemovedFromCartEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.ITEM_REMOVED_FROM_CART);
        eventDocument.setAggregateId(UUID.fromString(userId));
        eventDocument.setVersion(2);
        eventDocument.setData(itemRemovedFromCartEventJson());
        return eventDocument;
    }

    private EventDocument itemsOrderedEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.ITEMS_ORDERED);
        eventDocument.setAggregateId(UUID.fromString(userId));
        eventDocument.setVersion(3);
        eventDocument.setData(itemsOrderedEventJson());
        return eventDocument;
    }

    private String itemCreatedEventJson() {
        return "{\"eventType\":\"ITEM_CREATED\",\"itemId\":\"" + itemId + "\",\"name\":\"item\",\"userId\":\"" +
                userId + "\",\"version\":\"1\"}";
    }

    private String itemDeactivatedEventJson() {
        return "{\"eventType\":\"ITEM_DEACTIVATED\",\"itemId\":\"" + itemId + "\",\"userId\":\"" + userId +
                "\",\"version\":\"2\"}";
    }

    private String itemActivatedEventJson() {
        return "{\"eventType\":\"ITEM_ACTIVATED\",\"itemId\":\"" + itemId + "\",\"userId\":\"" + userId +
                "\",\"version\":\"3\"}";
    }

    private String itemAddedToCartEventJson() {
        return "{\"eventType\":\"ITEM_ADDED_TO_CART\",\"itemId\":\"" + itemId + "\",\"quantity\":\"2\",\"userId\":\"" +
                userId + "\",\"version\":\"1\"}";
    }

    private String itemRemovedFromCartEventJson() {
        return "{\"eventType\":\"ITEM_REMOVED_FROM_CART\",\"itemId\":\"" + itemId +
                "\",\"quantity\":\"1\",\"userId\":\"" + userId + "\",\"version\":\"2\"}";
    }

    private String itemsOrderedEventJson() {
        return "{\"eventType\":\"ITEMS_ORDERED\",\"userId\":\"" + userId + "\",\"version\":\"3\"}";
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public QueueMessagingTemplate queueMessagingTemplate(ResourceIdResolver resourceIdResolver) {
            return new QueueMessagingTemplate(
                    AmazonSQSAsyncClientBuilder.standard()
                            .withEndpointConfiguration(
                                    new AwsClientBuilder.EndpointConfiguration(
                                            LOCAL_STACK.getEndpointOverride(LocalStackContainer.Service.SNS).toString(),
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
