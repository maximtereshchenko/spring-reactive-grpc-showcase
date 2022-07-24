package com.github.xini1.apigateway.controller;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.apigateway.service.*;
import io.grpc.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

/**
 * @author Maxim Tereshchenko
 */
@RestController
public final class OrdersWriteController {

    private final UsersService usersService;
    private final OrdersWriteService ordersWriteService;
    private final StatusExceptionHandler handler = new StatusExceptionHandler();

    public OrdersWriteController(UsersService usersService, OrdersWriteService ordersWriteService) {
        this.usersService = usersService;
        this.ordersWriteService = ordersWriteService;
    }

    @PostMapping("/items")
    Mono<ResponseEntity<String>> createItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @RequestBody String name
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toCreateItemDto(name))
                .flatMap(ordersWriteService::create)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @PostMapping("/items/{id}/deactivate")
    Mono<ResponseEntity<Void>> deactivateItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @PathVariable String id
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toActivateDeactivateItemDto(id))
                .flatMap(ordersWriteService::deactivate)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @PostMapping("/items/{id}/activate")
    Mono<ResponseEntity<Void>> activateItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @PathVariable String id
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toActivateDeactivateItemDto(id))
                .flatMap(ordersWriteService::activate)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @PostMapping("/cart/add")
    Mono<ResponseEntity<Void>> addItemToCart(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @RequestBody AddRemoveItemToCartDto dto
    ) {
        return usersService.decode(jwt)
                .flatMap(userDto -> ordersWriteService.addItemToCart(userDto, dto))
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @PostMapping("/cart/remove")
    Mono<ResponseEntity<Void>> removeItemFromCart(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @RequestBody AddRemoveItemToCartDto dto
    ) {
        return usersService.decode(jwt)
                .flatMap(userDto -> ordersWriteService.removeItemFromCart(userDto, dto))
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @PostMapping("/cart/order")
    Mono<ResponseEntity<Void>> orderItems(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwt) {
        return usersService.decode(jwt)
                .flatMap(ordersWriteService::order)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }
}
