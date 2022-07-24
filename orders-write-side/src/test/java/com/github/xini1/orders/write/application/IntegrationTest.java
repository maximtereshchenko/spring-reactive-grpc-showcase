package com.github.xini1.orders.write.application;

import com.github.xini1.common.*;
import com.github.xini1.common.event.*;
import com.github.xini1.common.mongodb.*;
import com.github.xini1.orders.write.*;
import com.github.xini1.orders.write.rpc.*;
import io.grpc.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.kafka.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.utility.*;
import reactor.core.publisher.*;
import reactor.kafka.receiver.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.2.0")
    );

    static {
        MONGO_DB.start();
        KAFKA.start();
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
    private KafkaReceiver<UUID, String> kafkaReceiver;
    private String itemId;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB::getConnectionString);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
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
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(1)
                .first()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(itemId, itemCreatedEventJson());
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
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(2)
                .last()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(itemId, itemDeactivatedEventJson());
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
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(3)
                .last()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(itemId, itemActivatedEventJson());
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
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(4)
                .last()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(userId, itemAddedToCartEventJson());
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
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(5)
                .last()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(userId, itemRemovedFromCartEventJson());
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
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(6)
                .last()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(userId, itemsOrderedEventJson());
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
        KafkaReceiver<UUID, String> kafkaReceiver(KafkaProperties kafkaProperties) {
            return KafkaReceiver.create(
                    ReceiverOptions.<UUID, String>create(properties(kafkaProperties))
                            .subscription(List.of(Shared.EVENTS_KAFKA_TOPIC))
            );
        }

        private Map<String, Object> properties(KafkaProperties kafkaProperties) {
            return Map.of(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers(),
                    ConsumerConfig.GROUP_ID_CONFIG, "id",
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class,
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
            );
        }
    }
}
