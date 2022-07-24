package com.github.xini1.apigateway.controller;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.apigateway.service.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

/**
 * @author Maxim Tereshchenko
 */
@RestController
@RequestMapping("/items")
public final class OrdersReadController {

    private final OrdersReadService ordersReadService;

    public OrdersReadController(OrdersReadService ordersReadService) {
        this.ordersReadService = ordersReadService;
    }

    @GetMapping
    Flux<ItemDto> items() {
        return ordersReadService.items();
    }
}
