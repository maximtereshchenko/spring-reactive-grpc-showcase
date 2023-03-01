package com.github.xini1.users.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.EventType;
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
import org.springframework.http.HttpStatus;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Map;

import static com.github.xini1.Await.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Maxim Tereshchenko
 */
@SpringBootTest(classes = {Main.class, TestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    static {
        LOCAL_STACK.start();
        System.setProperty("aws.accessKeyId", LOCAL_STACK.getAccessKey());
        System.setProperty("aws.secretKey", LOCAL_STACK.getSecretKey());
    }

    private final UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build()
    );
    private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                            LOCAL_STACK.getEndpointOverride(Service.DYNAMODB).toString(),
                            LOCAL_STACK.getRegion()
                    )
            )
            .build();
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    @Autowired
    private WebTestClient webTestClient;
    private String userId;
    private String jwt;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("cloud.aws.region.static", LOCAL_STACK::getRegion);
        registry.add("application.sns.endpoint", () -> LOCAL_STACK.getEndpointOverride(Service.SNS).toString());
        registry.add(
                "application.dynamodb.endpoint",
                () -> LOCAL_STACK.getEndpointOverride(Service.DYNAMODB).toString()
        );
    }

    @Test
    @Order(0)
    void userCanRegister() {
        userId = stub.register(
                        RegisterRequest.newBuilder()
                                .setUsername("user")
                                .setPassword("pass")
                                .setUserType("REGULAR")
                                .build()
                )
                .getId();

        assertThat(userId).isNotNull();
        var result = amazonDynamoDB.scan(new ScanRequest().withTableName("events"));
        assertThat(result.getItems()).containsExactly(expectedEventDocument());
        await(() ->
                assertThat(queueMessagingTemplate.receiveAndConvert(Shared.ORDERS_READ_SIDE_SQS_QUEUE, String.class))
                        .isEqualTo(expectedEventJson())
        );
    }

    @Test
    @Order(1)
    void userCannotRegisterIfUsernameIsTaken() {
        var registerRequest = RegisterRequest.newBuilder()
                .setUsername("user")
                .setPassword("pass")
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
                                .setUsername("user")
                                .setPassword("pass")
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

    @Test
    void canPerformHealthCheck() {
        webTestClient.get().uri("/actuator/health").exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);
    }

    private Map<String, AttributeValue> expectedEventDocument() {
        return Map.of(
                "aggregateId", new AttributeValue().withS(userId),
                "version", new AttributeValue().withN("1"),
                "eventType", new AttributeValue().withS(EventType.USER_REGISTERED.toString()),
                "data", new AttributeValue().withS(expectedEventJson())
        );
    }

    private String expectedEventJson() {
        return "{\"eventType\":\"USER_REGISTERED\",\"userId\":\"" + userId +
                "\",\"username\":\"user\",\"version\":\"1\"}";
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
