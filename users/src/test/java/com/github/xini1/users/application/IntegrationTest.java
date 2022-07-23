package com.github.xini1.users.application;

import com.github.xini1.common.*;
import com.github.xini1.common.event.*;
import com.github.xini1.common.mongodb.*;
import com.github.xini1.users.*;
import com.github.xini1.users.rpc.*;
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

    private final UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build()
    );
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private KafkaReceiver<UUID, String> kafkaReceiver;
    private String userId;
    private String jwt;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", MONGO_DB::getHost);
        registry.add("spring.data.mongodb.port", MONGO_DB::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Test
    @Order(0)
    void userCanRegister() {
        userId = stub.register(
                        RegisterRequest.newBuilder()
                                .setUsername("username")
                                .setPassword("password")
                                .setUserType("REGULAR")
                                .build()
                )
                .getId();

        assertThat(userId).isNotNull();
        assertThat(eventRepository.findAll().collectList().block()).containsExactly(expectedEventDocument());
        assertThat(
                kafkaReceiver.receive()
                        .timeout(Duration.ofSeconds(1), Mono.empty())
                        .collectList()
                        .block()
        )
                .hasSize(1)
                .first()
                .extracting(record -> record.key().toString(), ConsumerRecord::value)
                .containsExactly(userId, expectedEventJson());
    }

    @Test
    @Order(1)
    void userCannotRegisterIfUsernameIsTaken() {
        var registerRequest = RegisterRequest.newBuilder()
                .setUsername("username")
                .setPassword("password")
                .setUserType("REGULAR")
                .build();

        assertThatThrownBy(() -> stub.register(registerRequest))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(Status::fromThrowable)
                .isEqualTo(Status.FAILED_PRECONDITION);
    }

    @Test
    @Order(2)
    void userCanLogin() {
        jwt = stub.login(
                        LoginRequest.newBuilder()
                                .setUsername("username")
                                .setPassword("password")
                                .build()
                )
                .getJwt();

        assertThat(jwt).isNotNull();
    }

    @Test
    @Order(3)
    void userCanBeIdentifiedByJwt() {
        assertThat(
                stub.decode(
                        DecodeJwtRequest.newBuilder()
                                .setJwt(jwt)
                                .build()
                )
        )
                .isEqualTo(
                        DecodedJwtResponse.newBuilder()
                                .setUserId(userId)
                                .setUserType("REGULAR")
                                .build()
                );
    }

    private EventDocument expectedEventDocument() {
        var eventDocument = new EventDocument();
        eventDocument.setEventType(EventType.USER_REGISTERED);
        eventDocument.setAggregateId(UUID.fromString(userId));
        eventDocument.setVersion(1);
        eventDocument.setData(expectedEventJson());
        return eventDocument;
    }

    private String expectedEventJson() {
        return "{\"eventType\":\"USER_REGISTERED\",\"userId\":\"" + userId +
                "\",\"username\":\"username\",\"version\":\"1\"}";
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
