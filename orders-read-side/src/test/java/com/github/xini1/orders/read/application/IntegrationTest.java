package com.github.xini1.orders.read.application;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xini1.common.Shared;
import com.github.xini1.common.event.Event;
import com.github.xini1.common.event.cart.ItemAddedToCart;
import com.github.xini1.common.event.cart.ItemRemovedFromCart;
import com.github.xini1.common.event.cart.ItemsOrdered;
import com.github.xini1.common.event.item.ItemActivated;
import com.github.xini1.common.event.item.ItemCreated;
import com.github.xini1.common.event.item.ItemDeactivated;
import com.github.xini1.orders.read.Main;
import com.github.xini1.orders.read.rpc.*;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.UUID;

import static com.github.xini1.Await.await;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Maxim Tereshchenko
 */
@SpringBootTest(
        classes = {IntegrationTest.TestConfig.class, Main.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
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

    private final OrderReadServiceGrpc.OrderReadServiceBlockingStub stub = OrderReadServiceGrpc.newBlockingStub(
            ManagedChannelBuilder.forAddress("localhost", 8080)
                    .usePlaintext()
                    .build()
    );
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID itemId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private NotificationMessagingTemplate notificationMessagingTemplate;
    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("cloud.aws.region.static", LOCAL_STACK::getRegion);
        registry.add(
                "application.sqs.endpoint",
                () -> LOCAL_STACK.getEndpointOverride(LocalStackContainer.Service.SQS).toString()
        );
        registry.add(
                "application.dynamodb.endpoint",
                () -> LOCAL_STACK.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString()
        );
    }

    @Test
    @Order(0)
    void canConsumeItemCreatedEvent() throws JsonProcessingException {
        emit(new ItemCreated(itemId, userId, "item", 1));

        await(() -> {
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
    void canConsumeItemDeactivatedEvent() throws JsonProcessingException {
        emit(new ItemDeactivated(itemId, userId, 2));

        await(() ->
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
    void canConsumeItemActivatedEvent() throws JsonProcessingException {
        emit(new ItemActivated(itemId, userId, 3));

        await(() ->
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
    void canConsumeItemAddedToCartEvent() throws JsonProcessingException {
        emit(new ItemAddedToCart(userId, itemId, 2, 1));

        await(() ->
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
    void canConsumeItemRemovedFromCartEvent() throws JsonProcessingException {
        emit(new ItemRemovedFromCart(userId, itemId, 1, 2));

        await(() ->
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
    void canConsumeItemsOrderedEvent() throws JsonProcessingException {
        emit(new ItemsOrdered(userId, 3));

        await(() -> {
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

    @Test
    void canPerformHealthCheck() {
        webTestClient.get().uri("/actuator/health").exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.OK);
    }

    private void emit(Event event) throws JsonProcessingException {
        notificationMessagingTemplate.convertAndSend(
                Shared.EVENTS_SNS_TOPIC,
                objectMapper.writeValueAsString(event.asMap())
        );
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public NotificationMessagingTemplate notificationMessagingTemplate(ResourceIdResolver resourceIdResolver) {
            return new NotificationMessagingTemplate(
                    AmazonSNSClientBuilder.standard()
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
