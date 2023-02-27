package com.github.xini1.apigateway.router;

import com.github.xini1.apigateway.dto.AddRemoveItemToCartDto;
import com.github.xini1.apigateway.service.OrdersWriteService;
import com.github.xini1.apigateway.service.UsersService;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
public final class OrdersWriteRouterFunction implements RouterFunction<ServerResponse> {

    private final RouterFunction<ServerResponse> original;

    public OrdersWriteRouterFunction(UsersService usersService, OrdersWriteService ordersWriteService) {
        original = RouterFunctions.route()
                .POST(
                        "/items",
                        request -> createItem(
                                usersService,
                                ordersWriteService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION),
                                request.bodyToMono(String.class)
                        )
                )
                .POST(
                        "/items/{id}/deactivate",
                        request -> deactivateItem(
                                usersService,
                                ordersWriteService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION),
                                request.pathVariable("id")
                        )
                )
                .POST(
                        "/items/{id}/activate",
                        request -> activateItem(
                                usersService,
                                ordersWriteService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION),
                                request.pathVariable("id")
                        )
                )
                .POST(
                        "/cart/add",
                        request -> addItemToCart(
                                usersService,
                                ordersWriteService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION),
                                request.bodyToMono(AddRemoveItemToCartDto.class)
                        )
                )
                .POST(
                        "/cart/remove",
                        request -> removeItemFromCart(
                                usersService,
                                ordersWriteService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION),
                                request.bodyToMono(AddRemoveItemToCartDto.class)
                        )
                )
                .POST(
                        "/cart/order",
                        request -> orderItems(
                                usersService,
                                ordersWriteService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION)
                        )
                )
                .filter((request, handler) ->
                        handler.handle(request)
                                .onErrorResume(
                                        StatusRuntimeException.class,
                                        e -> new StatusExceptionHandler().handle(e)
                                )
                )
                .build();
    }

    @Override
    public Mono<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return original.route(request);
    }

    private Mono<ServerResponse> orderItems(
            UsersService usersService,
            OrdersWriteService ordersWriteService,
            String jwt
    ) {
        return usersService.decode(jwt)
                .flatMap(ordersWriteService::order)
                .flatMap(ignored -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> removeItemFromCart(
            UsersService usersService,
            OrdersWriteService ordersWriteService,
            String jwt,
            Mono<AddRemoveItemToCartDto> dtoMono
    ) {
        return usersService.decode(jwt)
                .flatMap(userDto -> dtoMono.flatMap(dto -> ordersWriteService.removeItemFromCart(userDto, dto)))
                .flatMap(ignored -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> addItemToCart(
            UsersService usersService,
            OrdersWriteService ordersWriteService,
            String jwt,
            Mono<AddRemoveItemToCartDto> dtoMono
    ) {
        return usersService.decode(jwt)
                .flatMap(userDto -> dtoMono.flatMap(dto -> ordersWriteService.addItemToCart(userDto, dto)))
                .flatMap(ignored -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> activateItem(
            UsersService usersService,
            OrdersWriteService ordersWriteService,
            String jwt,
            String id
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toActivateDeactivateItemDto(id))
                .flatMap(ordersWriteService::activate)
                .flatMap(ignored -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> deactivateItem(
            UsersService usersService,
            OrdersWriteService ordersWriteService,
            String jwt,
            String id
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toActivateDeactivateItemDto(id))
                .flatMap(ordersWriteService::deactivate)
                .flatMap(ignored -> ServerResponse.ok().build());
    }

    private Mono<ServerResponse> createItem(
            UsersService usersService,
            OrdersWriteService ordersWriteService,
            String jwt,
            Mono<String> nameMono
    ) {
        return usersService.decode(jwt)
                .flatMap(userDto -> nameMono.map(userDto::toCreateItemDto))
                .flatMap(ordersWriteService::create)
                .flatMap(id -> ServerResponse.ok().bodyValue(id));
    }
}
