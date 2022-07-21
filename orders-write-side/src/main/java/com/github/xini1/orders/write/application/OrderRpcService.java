package com.github.xini1.orders.write.application;

import com.github.xini1.common.*;
import com.github.xini1.orders.write.exception.*;
import com.github.xini1.orders.write.rpc.*;
import com.github.xini1.orders.write.usecase.*;
import io.grpc.*;
import io.grpc.stub.*;
import org.slf4j.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
final class OrderRpcService extends OrderWriteServiceGrpc.OrderWriteServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderRpcService.class);

    private final ActivateItemUseCase activateItemUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final CreateItemUseCase createItemUseCase;
    private final DeactivateItemUseCase deactivateItemUseCase;
    private final OrderItemsInCartUseCase orderItemsInCartUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;

    OrderRpcService(
            ActivateItemUseCase activateItemUseCase,
            AddItemToCartUseCase addItemToCartUseCase,
            CreateItemUseCase createItemUseCase,
            DeactivateItemUseCase deactivateItemUseCase,
            OrderItemsInCartUseCase orderItemsInCartUseCase,
            RemoveItemFromCartUseCase removeItemFromCartUseCase
    ) {
        this.activateItemUseCase = activateItemUseCase;
        this.addItemToCartUseCase = addItemToCartUseCase;
        this.createItemUseCase = createItemUseCase;
        this.deactivateItemUseCase = deactivateItemUseCase;
        this.orderItemsInCartUseCase = orderItemsInCartUseCase;
        this.removeItemFromCartUseCase = removeItemFromCartUseCase;
    }

    @Override
    public void activate(ActivateItemRequest request, StreamObserver<Empty> responseObserver) {
        try {
            activateItemUseCase.activate(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType()),
                    UUID.fromString(request.getItemId())
            );
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (UserIsNotAdmin e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (ItemIsNotFound e) {
            log(e);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        } catch (ItemIsAlreadyActive e) {
            LOGGER.warn("Item is already active", e);
            responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not activate item", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    @Override
    public void add(AddItemToCartRequest request, StreamObserver<Empty> responseObserver) {
        try {
            addItemToCartUseCase.add(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType()),
                    UUID.fromString(request.getItemId()),
                    request.getQuantity()
            );
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (UserIsNotRegular e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (ItemIsNotFound e) {
            log(e);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        } catch (QuantityIsNotPositive | IllegalArgumentException e) {
            LOGGER.warn("Could not add item to cart", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        } catch (CouldNotAddDeactivatedItemToCart e) {
            LOGGER.warn("Item is deactivated", e);
            responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
        }
    }

    @Override
    public void create(CreateItemRequest request, StreamObserver<ItemIdResponse> responseObserver) {
        try {
            responseObserver.onNext(
                    ItemIdResponse.newBuilder()
                            .setItemId(
                                    createItemUseCase.create(
                                                    UUID.fromString(request.getUserId()),
                                                    UserType.valueOf(request.getUserType()),
                                                    request.getName()
                                            )
                                            .toString()
                            )
                            .build()
            );
            responseObserver.onCompleted();
        } catch (UserIsNotAdmin e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not create item", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    @Override
    public void deactivate(DeactivateItemRequest request, StreamObserver<Empty> responseObserver) {
        try {
            deactivateItemUseCase.deactivate(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType()),
                    UUID.fromString(request.getItemId())
            );
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (UserIsNotAdmin e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (ItemIsNotFound e) {
            log(e);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not deactivate item", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        } catch (ItemIsAlreadyDeactivated e) {
            LOGGER.warn("Item is already deactivated", e);
            responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
        }
    }

    @Override
    public void order(OrderItemsInCartRequest request, StreamObserver<Empty> responseObserver) {
        try {
            orderItemsInCartUseCase.order(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType())
            );
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (UserIsNotRegular e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (CartIsEmpty e) {
            LOGGER.warn("Cart is empty", e);
            responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not order items in cart", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    @Override
    public void remove(RemoveItemFromCartRequest request, StreamObserver<Empty> responseObserver) {
        try {
            removeItemFromCartUseCase.remove(
                    UUID.fromString(request.getUserId()),
                    UserType.valueOf(request.getUserType()),
                    UUID.fromString(request.getItemId()),
                    request.getQuantity()
            );
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (UserIsNotRegular e) {
            log(e);
            responseObserver.onError(new StatusException(Status.PERMISSION_DENIED));
        } catch (ItemIsNotFound e) {
            log(e);
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        } catch (QuantityIsMoreThanCartHas e) {
            LOGGER.warn("Cart has fewer items than user wants to remove", e);
            responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
        } catch (QuantityIsNotPositive | IllegalArgumentException e) {
            LOGGER.warn("Could not remove item from cart", e);
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT));
        }
    }

    private void log(ItemIsNotFound e) {
        LOGGER.warn("Item is not found", e);
    }

    private void log(UserIsNotAdmin e) {
        logInsufficientPermissions(e);
    }

    private void log(UserIsNotRegular e) {
        logInsufficientPermissions(e);
    }

    private void logInsufficientPermissions(RuntimeException e) {
        LOGGER.warn("Insufficient permissions", e);
    }
}
