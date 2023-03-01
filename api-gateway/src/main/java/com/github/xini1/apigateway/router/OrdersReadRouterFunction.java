package com.github.xini1.apigateway.router;

import com.github.xini1.apigateway.dto.ItemDto;
import com.github.xini1.apigateway.dto.TopOrderedItemDto;
import com.github.xini1.apigateway.service.OrdersReadService;
import com.github.xini1.apigateway.service.UsersService;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
public final class OrdersReadRouterFunction implements RouterFunction<ServerResponse> {

    private final RouterFunction<ServerResponse> original;

    public OrdersReadRouterFunction(UsersService usersService, OrdersReadService ordersReadService) {
        original = RouterFunctions.route()
                .GET(
                        "/items",
                        request -> items(ordersReadService)
                )
                .GET(
                        "/cart",
                        request -> cart(
                                usersService,
                                ordersReadService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION)
                        )
                )
                .GET(
                        "/orders",
                        request -> orders(
                                usersService,
                                ordersReadService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION)
                        )
                )
                .GET(
                        "/items/top",
                        request -> topOrderedItems(
                                usersService,
                                ordersReadService,
                                request.headers().firstHeader(HttpHeaders.AUTHORIZATION)
                        )
                )
                .filter((request, handler) ->
                        handler.handle(request)
                                .onErrorResume(
                                        StatusRuntimeException.class,
                                        e -> new StatusExceptionHandler().handle(request, e)
                                )
                )
                .build();
    }

    @Override
    public Mono<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return original.route(request);
    }

    private Mono<ServerResponse> items(OrdersReadService ordersReadService) {
        return ServerResponse.ok().body(ordersReadService.items(), ItemDto.class);
    }

    private Mono<ServerResponse> cart(UsersService usersService, OrdersReadService ordersReadService, String jwt) {
        return usersService.decode(jwt)
                .flatMap(ordersReadService::cart)
                .flatMap(cartDto -> ServerResponse.ok().bodyValue(cartDto));
    }

    private Mono<ServerResponse> orders(UsersService usersService, OrdersReadService ordersReadService, String jwt) {
        return usersService.decode(jwt)
                .flatMap(ordersReadService::orders)
                .flatMap(orderedItemsDto -> ServerResponse.ok().bodyValue(orderedItemsDto));
    }

    private Mono<ServerResponse> topOrderedItems(
            UsersService usersService,
            OrdersReadService ordersReadService,
            String jwt
    ) {
        return ServerResponse.ok()
                .body(
                        usersService.decode(jwt)
                                .flatMapMany(ordersReadService::topOrderedItems),
                        TopOrderedItemDto.class
                );
    }
}
