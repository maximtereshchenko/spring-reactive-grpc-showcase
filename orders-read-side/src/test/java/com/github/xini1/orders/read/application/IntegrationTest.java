package com.github.xini1.orders.read.application;

import com.github.xini1.common.*;
import com.github.xini1.common.event.*;
import com.github.xini1.common.event.cart.*;
import com.github.xini1.common.event.item.*;
import com.github.xini1.orders.read.*;
import com.github.xini1.orders.read.rpc.*;
import com.google.gson.*;
import io.grpc.*;
import org.apache.kafka.clients.producer.*;
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
import reactor.kafka.sender.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
