package com.github.xini1.orders.read.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.xini1.common.Shared;
import com.github.xini1.common.event.Event;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemActivated;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.orders.read.Main;
import com.github.xini1.orders.read.rpc.CartResponse;
import com.github.xini1.orders.read.rpc.Empty;
import com.github.xini1.orders.read.rpc.ItemInCartMessage;
import com.github.xini1.orders.read.rpc.ItemInOrderMessage;
import com.github.xini1.orders.read.rpc.ItemResponse;
import com.github.xini1.orders.read.rpc.OrderMessage;
import com.github.xini1.orders.read.rpc.OrderReadServiceGrpc;
import com.github.xini1.orders.read.rpc.OrderedItemsResponse;
import com.github.xini1.orders.read.rpc.TopOrderedItemResponse;
import com.github.xini1.orders.read.rpc.ViewCartRequest;
import com.github.xini1.orders.read.rpc.ViewOrderedItemsRequest;
import com.github.xini1.orders.read.rpc.ViewTopOrderedItemsRequest;
import com.google.gson.Gson;
import io.grpc.ManagedChannelBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * @author Maxim Tereshchenko
 */
@SpringBootTest(classes = {IntegrationTest.TestConfig.class, Main.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
final class IntegrationTest {

    @Container
    public static final MongoDBContainer MONGO_DB = new MongoDBContainer(
            DockerImageName.parse("mongo:5.0.9")
    );
    @Container
    public static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.2.0")
    );

    static {
        MONGO_DB.start();
        KAFKA.start();
    }

    private final OrderReadServiceGrpc.OrderReadServiceBlockingStub stub = OrderReadServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build()
    );
    private final Gson gson = new Gson();
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    @Autowired
    private KafkaSender<UUID, String> kafkaProducer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", MONGO_DB::getHost);
        registry.add("spring.data.mongodb.port", MONGO_DB::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Test
    @Order(0)
    void canConsumeItemCreatedEvent() {
        emit(new ItemCreated(itemId, userId, "item", 1));

        new Await().await(() -> {
            assertThat(stub.viewItems(Empty.newBuilder().build()))
                    .toIterable()
                    .containsExactly(
                            ItemResponse.newBuilder()
                                    .setId(itemId.toString())
                                    .setName("item")
                                    .setActive(true)
                                    .setVersion(1)
                                    .build()
                    );
            assertThat(
                    stub.viewTopOrderedItems(
                            ViewTopOrderedItemsRequest.newBuilder()
                                    .setUserType("ADMIN")
                                    .build()
                    )
            )
                    .toIterable()
                    .containsExactly(
                            TopOrderedItemResponse.newBuilder()
                                    .setId(itemId.toString())
                                    .setName("item")
                                    .setTimesOrdered(0)
                                    .build()
                    );
        });
    }

    @Test
    @Order(1)
    void canConsumeItemDeactivatedEvent() {
        emit(new ItemDeactivated(itemId, userId, 2));

        new Await().await(() ->
                assertThat(stub.viewItems(Empty.newBuilder().build()))
                        .toIterable()
                        .containsExactly(
                                ItemResponse.newBuilder()
                                        .setId(itemId.toString())
                                        .setName("item")
                                        .setActive(false)
                                        .setVersion(2)
                                        .build()
                        )
        );
    }

    @Test
    @Order(2)
    void canConsumeItemActivatedEvent() {
        emit(new ItemActivated(itemId, userId, 3));

        new Await().await(() ->
                assertThat(stub.viewItems(Empty.newBuilder().build()))
                        .toIterable()
                        .containsExactly(
                                ItemResponse.newBuilder()
                                        .setId(itemId.toString())
                                        .setName("item")
                                        .setActive(true)
                                        .setVersion(3)
                                        .build()
                        )
        );
    }

    @Test
    @Order(3)
    void canConsumeItemAddedToCartEvent() {
        emit(new ItemAddedToCart(userId, itemId, 2, 1));

        new Await().await(() ->
                assertThat(
                        stub.viewCart(
                                ViewCartRequest.newBuilder()
                                        .setUserId(userId.toString())
                                        .setUserType("REGULAR")
                                        .build()
                        )
                )
                        .isEqualTo(
                                CartResponse.newBuilder()
                                        .setUserId(userId.toString())
                                        .addItemsInCart(
                                                ItemInCartMessage.newBuilder()
                                                        .setId(itemId.toString())
                                                        .setName("item")
                                                        .setActive(true)
                                                        .setQuantity(2)
                                                        .setVersion(3)
                                                        .build()
                                        )
                                        .setVersion(1)
                                        .build()
                        )
        );
    }

    @Test
    @Order(4)
    void canConsumeItemRemovedFromCartEvent() {
        emit(new ItemRemovedFromCart(userId, itemId, 1, 2));

        new Await().await(() ->
                assertThat(
                        stub.viewCart(
                                ViewCartRequest.newBuilder()
                                        .setUserId(userId.toString())
                                        .setUserType("REGULAR")
                                        .build()
                        )
                )
                        .isEqualTo(
                                CartResponse.newBuilder()
                                        .setUserId(userId.toString())
                                        .addItemsInCart(
                                                ItemInCartMessage.newBuilder()
                                                        .setId(itemId.toString())
                                                        .setName("item")
                                                        .setActive(true)
                                                        .setQuantity(1)
                                                        .setVersion(3)
                                                        .build()
                                        )
                                        .setVersion(2)
                                        .build()
                        )
        );
    }

    @Test
    @Order(5)
    void canConsumeItemsOrderedEvent() {
        emit(new ItemsOrdered(userId, 3));

        new Await().await(() -> {
            assertThat(
                    stub.viewCart(
                            ViewCartRequest.newBuilder()
                                    .setUserId(userId.toString())
                                    .setUserType("REGULAR")
                                    .build()
                    )
            )
                    .isEqualTo(
                            CartResponse.newBuilder()
                                    .setUserId(userId.toString())
                                    .setVersion(3)
                                    .build()
                    );
            assertThat(
                    stub.viewOrderedItems(
                            ViewOrderedItemsRequest.newBuilder()
                                    .setUserId(userId.toString())
                                    .setUserType("REGULAR")
                                    .build()
                    )
            )
                    .isEqualTo(
                            OrderedItemsResponse.newBuilder()
                                    .setUserId(userId.toString())
                                    .addOrders(
                                            OrderMessage.newBuilder()
                                                    .setTimestamp("2020-01-01T01:00:00Z")
                                                    .addItems(
                                                            ItemInOrderMessage.newBuilder()
                                                                    .setId(itemId.toString())
                                                                    .setQuantity(1)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    );
            assertThat(
                    stub.viewTopOrderedItems(
                            ViewTopOrderedItemsRequest.newBuilder()
                                    .setUserType("ADMIN")
                                    .build()
                    )
            )
                    .toIterable()
                    .containsExactly(
                            TopOrderedItemResponse.newBuilder()
                                    .setId(itemId.toString())
                                    .setName("item")
                                    .setTimesOrdered(1)
                                    .build()
                    );
        });
    }

    private void emit(Event event) {
        kafkaProducer.send(
                        Mono.just(
                                SenderRecord.create(
                                        new ProducerRecord<>(
                                                Shared.EVENTS_KAFKA_TOPIC,
                                                event.aggregateId(),
                                                gson.toJson(event.asMap())
                                        ),
                                        event.aggregateId()
                                )
                        )
                )
                .subscribe();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        KafkaSender<UUID, String> kafkaProducer(KafkaProperties kafkaProperties) {
            return KafkaSender.create(SenderOptions.create(properties(kafkaProperties)));
        }

        @Bean
        Clock fixed() {
            return Clock.fixed(
                    LocalDateTime.of(2020, 1, 1, 1, 0)
                            .toInstant(ZoneOffset.UTC),
                    ZoneOffset.UTC
            );
        }

        private Map<String, Object> properties(KafkaProperties kafkaProperties) {
            return Map.of(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers(),
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
            );
        }
    }
}
