package com.github.xini1.apigateway.controller;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.apigateway.service.*;
import io.grpc.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
@RestController
public final class OrdersReadController {

    private final UsersService usersService;
    private final OrdersReadService ordersReadService;
    private final StatusExceptionHandler handler = new StatusExceptionHandler();

    public OrdersReadController(UsersService usersService, OrdersReadService ordersReadService) {
        this.usersService = usersService;
        this.ordersReadService = ordersReadService;
    }

    @GetMapping("/items")
    ResponseEntity<Flux<ItemDto>> items() {
        return ResponseEntity.ok(ordersReadService.items());
    }

    @GetMapping("/cart")
    Mono<ResponseEntity<CartDto>> cart(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwt) {
        return usersService.decode(jwt)
                .flatMap(ordersReadService::cart)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @GetMapping("/orders")
    Mono<ResponseEntity<OrderedItemsDto>> orders(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwt) {
        return usersService.decode(jwt)
                .flatMap(ordersReadService::orders)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }

    @GetMapping("/items/top")
    Mono<ResponseEntity<List<TopOrderedItemDto>>> topOrderedItems(@RequestHeader(HttpHeaders.AUTHORIZATION) String jwt) {
        return usersService.decode(jwt)
                .flatMapMany(ordersReadService::topOrderedItems)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, handler::handle);
    }
}
