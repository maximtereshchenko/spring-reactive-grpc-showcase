package com.github.xini1.apigateway;

import com.github.xini1.apigateway.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.*;
import org.springframework.test.context.*;
import org.springframework.test.web.reactive.server.*;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.utility.*;

import java.time.*;
import java.util.*;

import static com.github.xini1.Await.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Maxim Tereshchenko
 */
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
final class IntegrationTest {

    private static final Network NETWORK = Network.newNetwork();
    @Container
    private static final MongoDBContainer MONGO_DB = new MongoDBContainer(
            DockerImageName.parse("mongo:5.0.9")
    )
            .withNetwork(NETWORK)
            .withNetworkAliases("database");
    @Container
    private static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.2.0")
    )
            .withNetwork(NETWORK)
            .withNetworkAliases("kafka");
    @Container
    private static final GenericContainer<?> USERS = container("users");
    @Container
    private static final GenericContainer<?> ORDERS_WRITE_SIDE = container("orders-write-side");
    @Container
    private static final GenericContainer<?> ORDERS_READ_SIDE = container("orders-read-side");

    static {
        MONGO_DB.start();
        KAFKA.start();
        start(USERS, ORDERS_READ_SIDE, ORDERS_WRITE_SIDE);
    }

    private final WebTestClient webClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:8080")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .responseTimeout(Duration.ofSeconds(15))
            .build();
    private String regularUserJwt;
    private String adminUserJwt;
    private String userId;
    private String itemId;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("application.rpc.users.address", USERS::getHost);
        registry.add("application.rpc.users.port", USERS::getFirstMappedPort);
        registry.add("application.rpc.orders.write.address", ORDERS_WRITE_SIDE::getHost);
        registry.add("application.rpc.orders.write.port", ORDERS_WRITE_SIDE::getFirstMappedPort);
        registry.add("application.rpc.orders.read.address", ORDERS_READ_SIDE::getHost);
        registry.add("application.rpc.orders.read.port", ORDERS_READ_SIDE::getFirstMappedPort);
    }

    private static GenericContainer<?> container(String name) {
        return new GenericContainer<>(DockerImageName.parse(name))
                .withNetwork(NETWORK)
                .withNetworkAliases(name)
                .withExposedPorts(8080);
    }

    private static void start(GenericContainer<?>... containers) {
        for (GenericContainer<?> container : containers) {
            container.withEnv("SPRING_DATA_MONGODB_HOST", "database")
                    .withEnv("SPRING_DATA_MONGODB_PORT", "27017")
                    .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
                    .start();
        }
    }

    @Test
    @Order(0)
    void userCanRegister() {
        var response = webClient.post()
                .uri("/users")
                .bodyValue(registerRegularUserDto())
                .exchange()
                .returnResult(String.class);
        userId = response.getResponseBody().blockFirst();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(userId).isNotNull();
    }

    @Test
    @Order(1)
    void userCanLogin() {
        var response = webClient.post()
                .uri("/users/login")
                .bodyValue(loginRegularUserDto())
                .exchange()
                .returnResult(String.class);
        regularUserJwt = response.getResponseBody().blockFirst();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(regularUserJwt).isNotNull();
    }

    @Test
    @Order(2)
    void adminCanRegister() {
        var response = webClient.post()
                .uri("/users")
                .bodyValue(registerAdminDto())
                .exchange()
                .returnResult(String.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(response.getResponseBody().blockFirst()).isNotNull();
    }

    @Test
    @Order(3)
    void adminCanLogin() {
        var response = webClient.post()
                .uri("/users/login")
                .bodyValue(loginAdminDto())
                .exchange()
                .returnResult(String.class);
        adminUserJwt = response.getResponseBody().blockFirst();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(adminUserJwt).isNotNull();
    }

    @Test
    @Order(4)
    void adminCanCreateItem() {
        var createItemResponse = webClient.post()
                .uri("/items")
                .header(HttpHeaders.AUTHORIZATION, adminUserJwt)
                .bodyValue("item")
                .exchange()
                .returnResult(String.class);
        itemId = createItemResponse.getResponseBody().blockFirst();

        assertThat(createItemResponse.getStatus()).isEqualTo(HttpStatus.OK);

        await(() -> {
            var itemsResponse = webClient.get()
                    .uri("/items")
                    .exchange()
                    .returnResult(ItemDto.class);

            assertThat(itemsResponse.getStatus()).isEqualTo(HttpStatus.OK);
            assertThat(
                    itemsResponse.getResponseBody()
                            .collectList()
                            .block()
            )
                    .containsExactly(expectedItemDto());
        });
    }

    @Test
    @Order(5)
    void adminCanDeactivateItem() {
        var deactivateItemResponse = webClient.post()
                .uri("/items/{itemId}/deactivate", itemId)
                .header(HttpHeaders.AUTHORIZATION, adminUserJwt)
                .exchange()
                .returnResult(Void.class);

        assertThat(deactivateItemResponse.getStatus()).isEqualTo(HttpStatus.OK);

        await(() -> {
            var itemsResponse = webClient.get()
                    .uri("/items")
                    .exchange()
                    .returnResult(ItemDto.class);

            assertThat(itemsResponse.getStatus()).isEqualTo(HttpStatus.OK);
            assertThat(
                    itemsResponse.getResponseBody()
                            .collectList()
                            .block()
            )
                    .containsExactly(expectedDeactivatedItemDto());
        });
    }

    @Test
    @Order(6)
    void adminCanActivateItem() {
        var activateItemResponse = webClient.post()
                .uri("/items/{itemId}/activate", itemId)
                .header(HttpHeaders.AUTHORIZATION, adminUserJwt)
                .exchange()
                .returnResult(Void.class);

        assertThat(activateItemResponse.getStatus()).isEqualTo(HttpStatus.OK);

        await(() -> {
            var itemsResponse = webClient.get()
                    .uri("/items")
                    .exchange()
                    .returnResult(ItemDto.class);

            assertThat(itemsResponse.getStatus()).isEqualTo(HttpStatus.OK);
            assertThat(
                    itemsResponse.getResponseBody()
                            .collectList()
                            .block()
            )
                    .containsExactly(expectedActivatedItemDto());
        });
    }

    @Test
    @Order(7)
    void userCanAddItemToCart() {
        var addItemToCartResponse = webClient.post()
                .uri("/cart")
                .header(HttpHeaders.AUTHORIZATION, regularUserJwt)
                .bodyValue(addItemToCartDto())
                .exchange()
                .returnResult(Void.class);

        assertThat(addItemToCartResponse.getStatus()).isEqualTo(HttpStatus.OK);

        await(() -> {
            var cartResponse = webClient.get()
                    .uri("/cart")
                    .header(HttpHeaders.AUTHORIZATION, regularUserJwt)
                    .exchange()
                    .returnResult(CartDto.class);

            assertThat(cartResponse.getStatus()).isEqualTo(HttpStatus.OK);
            assertThat(
                    cartResponse.getResponseBody()
                            .blockFirst()
            )
                    .isEqualTo(expectedCartDto());
        });
    }

    private AddItemToCartDto addItemToCartDto() {
        var dto = new AddItemToCartDto();
        dto.setItemId(itemId);
        dto.setQuantity(2);
        return dto;
    }

    private CartDto expectedCartDto() {
        var itemInCart = new CartDto.ItemInCartDto();
        itemInCart.setId(itemId);
        itemInCart.setName("item");
        itemInCart.setActive(true);
        itemInCart.setQuantity(2);
        itemInCart.setVersion(3);
        var cart = new CartDto();
        cart.setUserId(userId);
        cart.setItemsInCart(List.of(itemInCart));
        cart.setVersion(1);
        return cart;
    }

    private ItemDto expectedActivatedItemDto() {
        return itemDto(true, 3);
    }

    private ItemDto expectedDeactivatedItemDto() {
        return itemDto(false, 2);
    }

    private ItemDto expectedItemDto() {
        return itemDto(true, 1);
    }

    private ItemDto itemDto(boolean active, int version) {
        var dto = new ItemDto();
        dto.setId(itemId);
        dto.setName("item");
        dto.setActive(active);
        dto.setVersion(version);
        return dto;
    }

    private LoginDto loginRegularUserDto() {
        var dto = new LoginDto();
        dto.setUsername("user");
        dto.setPassword("pass");
        return dto;
    }

    private LoginDto loginAdminDto() {
        var dto = new LoginDto();
        dto.setUsername("admin");
        dto.setPassword("pass");
        return dto;
    }

    private RegisterUserDto registerRegularUserDto() {
        var dto = new RegisterUserDto();
        dto.setUsername("user");
        dto.setPassword("pass");
        dto.setUserType("REGULAR");
        return dto;
    }

    private RegisterUserDto registerAdminDto() {
        var dto = new RegisterUserDto();
        dto.setUsername("admin");
        dto.setPassword("pass");
        dto.setUserType("ADMIN");
        return dto;
    }

    @TestConfiguration
    static class TestConfig {

    }
}
