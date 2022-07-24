package com.github.xini1.apigateway.controller;

import com.github.xini1.apigateway.service.*;
import io.grpc.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

/**
 * @author Maxim Tereshchenko
 */
@RestController
@RequestMapping("/items")
public final class OrdersWriteController {

    private final UsersService usersService;
    private final OrdersWriteService ordersWriteService;

    public OrdersWriteController(UsersService usersService, OrdersWriteService ordersWriteService) {
        this.usersService = usersService;
        this.ordersWriteService = ordersWriteService;
    }

    @PostMapping
    Mono<ResponseEntity<String>> createItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @RequestBody String name
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toCreateItemDto(name))
                .flatMap(ordersWriteService::create)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, this::responseEntityMono);
    }

    @PostMapping("/{id}/deactivate")
    Mono<ResponseEntity<Void>> deactivateItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @PathVariable String id
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toDeactivateItemDto(id))
                .flatMap(ordersWriteService::deactivate)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, this::responseEntityMono);
    }

    private <T> Mono<ResponseEntity<T>> responseEntityMono(StatusRuntimeException e) {
        return Mono.just(responseEntity(e));
    }

    private <T> ResponseEntity<T> responseEntity(StatusRuntimeException e) {
        switch (e.getStatus().getCode()) {
            case PERMISSION_DENIED:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case NOT_FOUND:
                ResponseEntity.notFound().build();
            default:
                return ResponseEntity.badRequest().build();
        }
    }
}
