package com.github.xini1.users.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.EventType;
import com.github.xini1.common.mongodb.EventDocument;
import com.github.xini1.users.Main;
import com.github.xini1.users.application.IntegrationTest.TestConfig;
import com.github.xini1.users.rpc.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Maxim Tereshchenko
 */
@SpringBootTest(classes = {Main.class, TestConfig.class})
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
            .withServices(Service.SNS, Service.SQS)
            .withFileSystemBind(
                    "../localstack-setup.sh",
                    "/etc/localstack/init/ready.d/localstack-setup.sh"
            );

    static {
        MONGO_DB.start();
        LOCAL_STACK.start();
    }

    private final UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build()
    );
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    private String userId;
    private String jwt;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB::getReplicaSetUrl);
        registry.add("cloud.aws.region.static", LOCAL_STACK::getRegion);
        registry.add("application.sns.endpoint", () -> LOCAL_STACK.getEndpointOverride(Service.SNS).toString());
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
        assertThat(queueMessagingTemplate.receiveAndConvert(Shared.USERS_SQS_QUEUE, String.class))
                .isEqualTo(expectedEventJson());
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
