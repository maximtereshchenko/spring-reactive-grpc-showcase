package com.github.xini1.apigateway.service;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.orders.read.rpc.*;
import io.grpc.*;
import reactor.core.publisher.*;

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
}
