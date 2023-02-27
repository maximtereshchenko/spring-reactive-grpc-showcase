package com.github.xini1.apigateway.service;

import com.github.xini1.apigateway.dto.ActivateDeactivateItemDto;
import com.github.xini1.apigateway.dto.AddRemoveItemToCartDto;
import com.github.xini1.apigateway.dto.CreateItemDto;
import com.github.xini1.apigateway.dto.UserDto;
import com.github.xini1.orders.write.rpc.ItemIdResponse;
import com.github.xini1.orders.write.rpc.OrderWriteServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
public final class OrdersWriteService {

    private final OrderWriteServiceGrpc.OrderWriteServiceStub orderWriteServiceGrpc;

    public OrdersWriteService(String address, int port) {
        orderWriteServiceGrpc = OrderWriteServiceGrpc.newStub(
                ManagedChannelBuilder.forAddress(address, port)
                        .usePlaintext()
                        .build()
        );
    }

    public Mono<String> create(CreateItemDto dto) {
        return Mono.create(sink ->
                orderWriteServiceGrpc.create(
                        dto.toCreateItemRequest(),
                        new MonoStreamObserverAdapter<>(sink, ItemIdResponse::getItemId)
                )
        );
    }

    public Mono<Void> deactivate(ActivateDeactivateItemDto dto) {
        return Mono.create(sink ->
                orderWriteServiceGrpc.deactivate(
                        dto.toDeactivateItemRequest(),
                        new VoidMonoStreamObserverAdapter<>(sink)
                )
        );
    }

    public Mono<Void> activate(ActivateDeactivateItemDto dto) {
        return Mono.create(sink ->
                orderWriteServiceGrpc.activate(
                        dto.toActivateItemRequest(),
                        new VoidMonoStreamObserverAdapter<>(sink)
                )
        );
    }

    public Mono<Void> addItemToCart(UserDto userDto, AddRemoveItemToCartDto addItemToCartDto) {
        return Mono.create(sink ->
                orderWriteServiceGrpc.add(
                        userDto.toAddItemToCartRequest(addItemToCartDto),
                        new VoidMonoStreamObserverAdapter<>(sink)
                )
        );
    }

    public Mono<Void> removeItemFromCart(UserDto userDto, AddRemoveItemToCartDto removeItemToCartDto) {
        return Mono.create(sink ->
                orderWriteServiceGrpc.remove(
                        userDto.toRemoveItemFromCartRequest(removeItemToCartDto),
                        new VoidMonoStreamObserverAdapter<>(sink)
                )
        );
    }

    public Mono<Void> order(UserDto userDto) {
        return Mono.create(sink ->
                orderWriteServiceGrpc.order(
                        userDto.toOrderItemsInCartRequest(),
                        new VoidMonoStreamObserverAdapter<>(sink)
                )
        );
    }
}
