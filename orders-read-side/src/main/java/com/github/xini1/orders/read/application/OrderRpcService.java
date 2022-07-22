package com.github.xini1.orders.read.application;

import com.github.xini1.common.*;
import com.github.xini1.orders.read.exception.*;
import com.github.xini1.orders.read.rpc.*;
import com.github.xini1.orders.read.usecase.*;
import com.github.xini1.orders.read.view.*;
import io.grpc.*;
import io.grpc.stub.*;
import org.slf4j.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Maxim Tereshchenko
 */
final class OrderRpcService extends OrderReadServiceGrpc.OrderReadServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRpcService.class);

    private final ViewCartUseCase viewCartUseCase;
    private final ViewItemsUseCase viewItemsUseCase;
    private final ViewOrderedItemsUseCase viewOrderedItemsUseCase;
    private final ViewTopOrderedItemsUseCase viewTopOrderedItemsUseCase;

    public OrderRpcService(
            ViewCartUseCase viewCartUseCase,
            ViewItemsUseCase viewItemsUseCase,
            ViewOrderedItemsUseCase viewOrderedItemsUseCase,
            ViewTopOrderedItemsUseCase viewTopOrderedItemsUseCase
    ) {
        this.viewCartUseCase = viewCartUseCase;
        this.viewItemsUseCase = viewItemsUseCase;
        this.viewOrderedItemsUseCase = viewOrderedItemsUseCase;
        this.viewTopOrderedItemsUseCase = viewTopOrderedItemsUseCase;
    }

    @Override
    public void viewCart(ViewCartRequest request, StreamObserver<CartResponse> responseObserver) {
        try {
            var cart = viewCartUseCase.view(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType())
            );
            responseObserver.onNext(
                    CartResponse.newBuilder()
                            .setUserId(cart.getUserId().toString())
                            .addAllItemsInCart(
                                    cart.getItemsInCart()
                                            .stream()
                                            .map(itemInCart ->
                                                    ItemInCartMessage.newBuilder()
                                                            .setId(itemInCart.getId().toString())
                                                            .setName(itemInCart.getName())
                                                            .setActive(itemInCart.isActive())
                                                            .setQuantity(itemInCart.getQuantity())
                                                            .setVersion(itemInCart.getVersion())
                                                            .build()
                                            )
                                            .collect(Collectors.toList())
                            )
                            .setVersion(cart.getVersion())
                            .build()
            );
            responseObserver.onCompleted();
        } catch (UserIsNotRegular e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not view cart", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    @Override
    public void viewItems(Empty request, StreamObserver<ItemResponse> responseObserver) {
        viewItemsUseCase.view()
                .forEach(item ->
                        responseObserver.onNext(
                                ItemResponse.newBuilder()
                                        .setId(item.getId().toString())
                                        .setName(item.getName())
                                        .setActive(item.isActive())
                                        .setVersion(item.getVersion())
                                        .build()
                        )
                );
        responseObserver.onCompleted();
    }

    @Override
    public void viewOrderedItems(
            ViewOrderedItemsRequest request,
            StreamObserver<OrderedItemsResponse> responseObserver
    ) {
        try {
            var orderedItems = viewOrderedItemsUseCase.viewOrderedItems(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType())
            );
            responseObserver.onNext(
                    OrderedItemsResponse.newBuilder()
                            .setUserId(orderedItems.getUserId().toString())
                            .addAllOrders(
                                    orderedItems.getOrders()
                                            .stream()
                                            .map(order ->
                                                    OrderMessage.newBuilder()
                                                            .setTimestamp(order.getTimestamp().toString())
                                                            .addAllItems(
                                                                    order.getItems()
                                                                            .stream()
                                                                            .map(this::itemInOrderMessage)
                                                                            .collect(Collectors.toList())
                                                            )
                                                            .build()
                                            )
                                            .collect(Collectors.toList())
                            )
                            .build()
            );
            responseObserver.onCompleted();
        } catch (UserIsNotRegular e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not view ordered items", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    @Override
    public void viewTopOrderedItems(
            ViewTopOrderedItemsRequest request,
            StreamObserver<TopOrderedItemResponse> responseObserver
    ) {
        try {
            viewTopOrderedItemsUseCase.view(UserType.valueOf(request.getUserType()))
                    .forEach(topOrderedItem ->
                            responseObserver.onNext(
                                    TopOrderedItemResponse.newBuilder()
                                            .setId(topOrderedItem.getId().toString())
                                            .setName(topOrderedItem.getName())
                                            .setTimesOrdered(topOrderedItem.getTimesOrdered())
                                            .build()
                            )
                    );
            responseObserver.onCompleted();
        } catch (UserIsNotAdmin e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not view top ordered items", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    private void log(RuntimeException e) {
        LOGGER.warn("Insufficient permissions", e);
    }

    private ItemInOrderMessage itemInOrderMessage(OrderedItems.ItemInOrder itemInOrder) {
        return ItemInOrderMessage.newBuilder()
                .setId(itemInOrder.getId().toString())
                .setQuantity(itemInOrder.getQuantity())
                .build();
    }
}
