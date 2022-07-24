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
    private final OrdersWriteService itemsService;

    public OrdersWriteController(UsersService usersService, OrdersWriteService itemsService) {
        this.usersService = usersService;
        this.itemsService = itemsService;
    }

    @PostMapping
    Mono<ResponseEntity<String>> createItem(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String jwt,
            @RequestBody String name
    ) {
        return usersService.decode(jwt)
                .map(userDto -> userDto.toCreateItemDto(name))
                .flatMap(itemsService::create)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, this::responseEntityMono);
    }

    private Mono<ResponseEntity<String>> responseEntityMono(StatusRuntimeException e) {
        return Mono.just(responseEntity(e));
    }

    private ResponseEntity<String> responseEntity(StatusRuntimeException e) {
        if (e.getStatus() == Status.PERMISSION_DENIED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.badRequest().build();
    }
}
