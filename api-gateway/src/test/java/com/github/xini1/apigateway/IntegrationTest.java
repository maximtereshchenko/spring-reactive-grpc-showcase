package com.github.xini1.apigateway;

import com.github.xini1.apigateway.dto.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.List;

import static com.github.xini1.Await.await;
import static org.assertj.core.api.Assertions.assertThat;

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
            .withNetwork(NETWORK)
            .withNetworkAliases("localstack");
    @Container
    private static final GenericContainer<?> USERS = container("users");
    @Container
    private static final GenericContainer<?> ORDERS_WRITE_SIDE = container("orders-write-side");
    @Container
    private static final GenericContainer<?> ORDERS_READ_SIDE = container("orders-read-side")
            .withEnv("SPRING_PROFILES_ACTIVE", "test");

    static {
        LOCAL_STACK.start();
        USERS.start();
        ORDERS_READ_SIDE.start();
        ORDERS_WRITE_SIDE.start();
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
                .dependsOn(LOCAL_STACK)
                .withNetwork(NETWORK)
                .withNetworkAliases(name)
                .withExposedPorts(8080)
                .withEnv("AWS_ACCESS_KEY_ID", "whatever")
                .withEnv("AWS_SECRET_KEY", "whatever");
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
                    .containsExactly(expectedItem());
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
                    .containsExactly(expectedDeactivatedItem());
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
                    .containsExactly(expectedActivatedItem());
        });
    }

    @Test
    @Order(7)
    void userCanAddItemToCart() {
        var addItemToCartResponse = webClient.post()
                .uri("/cart/add")
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
                    .isEqualTo(expectedCart());
        });
    }

    @Test
    @Order(8)
    void userCanRemoveItemFromCart() {
        var removeItemFromCartResponse = webClient.post()
                .uri("/cart/remove")
                .header(HttpHeaders.AUTHORIZATION, regularUserJwt)
                .bodyValue(removeItemFromCartDto())
                .exchange()
                .returnResult(Void.class);

        assertThat(removeItemFromCartResponse.getStatus()).isEqualTo(HttpStatus.OK);

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
                    .isEqualTo(expectedAfterRemovalCart());
        });
    }

    @Test
    @Order(9)
    void userCanOrderItems() {
        var orderItemsResponse = webClient.post()
                .uri("/cart/order")
                .header(HttpHeaders.AUTHORIZATION, regularUserJwt)
                .exchange()
                .returnResult(Void.class);

        assertThat(orderItemsResponse.getStatus()).isEqualTo(HttpStatus.OK);

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
                    .isEqualTo(emptyCartDto());

            var orderedItemsResponse = webClient.get()
                    .uri("/orders")
                    .header(HttpHeaders.AUTHORIZATION, regularUserJwt)
                    .exchange()
                    .returnResult(OrderedItemsDto.class);
            assertThat(orderedItemsResponse.getStatus()).isEqualTo(HttpStatus.OK);
            assertThat(
                    orderedItemsResponse.getResponseBody()
                            .blockFirst()
            )
                    .isEqualTo(expectedOrderedItems());

            var topOrderedItemsResponse = webClient.get()
                    .uri("/items/top")
                    .header(HttpHeaders.AUTHORIZATION, adminUserJwt)
                    .exchange()
                    .returnResult(TopOrderedItemDto.class);
            assertThat(topOrderedItemsResponse.getStatus()).isEqualTo(HttpStatus.OK);
            assertThat(
                    topOrderedItemsResponse.getResponseBody()
                            .collectList()
                            .block()
            )
                    .containsExactly(expectedTopOrderedItem());
        });
    }

    private TopOrderedItemDto expectedTopOrderedItem() {
        var dto = new TopOrderedItemDto();
        dto.setId(itemId);
        dto.setName("item");
        dto.setTimesOrdered(1);
        return dto;
    }

    private OrderedItemsDto expectedOrderedItems() {
        var itemInOrderDto = new OrderedItemsDto.ItemInOrderDto();
        itemInOrderDto.setId(itemId);
        itemInOrderDto.setQuantity(1);
        var orderDto = new OrderedItemsDto.OrderDto();
        orderDto.setTimestamp("2020-01-01T01:00:00Z");
        orderDto.setItems(List.of(itemInOrderDto));
        var orderedItemsDto = new OrderedItemsDto();
        orderedItemsDto.setUserId(userId);
        orderedItemsDto.setOrders(List.of(orderDto));
        return orderedItemsDto;
    }

    private CartDto emptyCartDto() {
        var cart = new CartDto();
        cart.setUserId(userId);
        cart.setVersion(3);
        return cart;
    }

    private AddRemoveItemToCartDto removeItemFromCartDto() {
        return addRemoveItemToCartDto(1);
    }

    private AddRemoveItemToCartDto addItemToCartDto() {
        return addRemoveItemToCartDto(2);
    }

    private AddRemoveItemToCartDto addRemoveItemToCartDto(int quantity) {
        var dto = new AddRemoveItemToCartDto();
        dto.setItemId(itemId);
        dto.setQuantity(quantity);
        return dto;
    }

    private CartDto expectedAfterRemovalCart() {
        return cartDto(1, 2);
    }

    private CartDto expectedCart() {
        return cartDto(2, 1);
    }

    private CartDto cartDto(int quantity, long version) {
        var itemInCart = new CartDto.ItemInCartDto();
        itemInCart.setId(itemId);
        itemInCart.setName("item");
        itemInCart.setActive(true);
        itemInCart.setQuantity(quantity);
        itemInCart.setVersion(3);
        var cart = new CartDto();
        cart.setUserId(userId);
        cart.setItemsInCart(List.of(itemInCart));
        cart.setVersion(version);
        return cart;
    }

    private ItemDto expectedActivatedItem() {
        return itemDto(true, 3);
    }

    private ItemDto expectedDeactivatedItem() {
        return itemDto(false, 2);
    }

    private ItemDto expectedItem() {
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
}
