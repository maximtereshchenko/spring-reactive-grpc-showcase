package com.github.xini1.apigateway.service;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.orders.read.rpc.Empty;
import com.github.xini1.orders.read.rpc.OrderReadServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
public final class OrdersReadService {

    private final OrderReadServiceGrpc.OrderReadServiceStub orderReadServiceGrpc;

    public OrdersReadService(String address, int port) {
        orderReadServiceGrpc = OrderReadServiceGrpc.newStub(
                ManagedChannelBuilder.forAddress(address, port)
                        .usePlaintext()
                        .build()
        );
    }

    public Flux<ItemDto> items() {
        return Flux.create(sink ->
                orderReadServiceGrpc.viewItems(
                        Empty.newBuilder().build(),
                        new FluxStreamObserverAdapter<>(sink, ItemDto::new)
                )
        );
    }

    public Mono<CartDto> cart(UserDto userDto) {
        return Mono.create(sink ->
                orderReadServiceGrpc.viewCart(
                        userDto.toViewCartRequest(),
                        new MonoStreamObserverAdapter<>(sink, CartDto::new)
                )
        );
    }

    public Mono<OrderedItemsDto> orders(UserDto userDto) {
        return Mono.create(sink ->
                orderReadServiceGrpc.viewOrderedItems(
                        userDto.toViewOrderedItemsRequest(),
                        new MonoStreamObserverAdapter<>(sink, OrderedItemsDto::new)
                )
        );
    }

    public Flux<TopOrderedItemDto> topOrderedItems(UserDto userDto) {
        return Flux.create(sink ->
                orderReadServiceGrpc.viewTopOrderedItems(
                        userDto.toViewTopOrderedItemsRequest(),
                        new FluxStreamObserverAdapter<>(sink, TopOrderedItemDto::new)
                )
        );
    }
}
