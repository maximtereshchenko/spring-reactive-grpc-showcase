package com.github.xini1.apigateway.service;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.orders.write.rpc.*;
import io.grpc.*;
import reactor.core.publisher.*;

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
}
