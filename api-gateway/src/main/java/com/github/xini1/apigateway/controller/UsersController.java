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
@RequestMapping("/users")
public final class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    Mono<ResponseEntity<UUID>> register(@RequestBody RegisterUserDto dto) {
        return usersService.register(dto)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/login")
    Mono<ResponseEntity<String>> login(@RequestBody LoginDto dto) {
        return usersService.login(dto)
                .map(ResponseEntity::ok)
                .onErrorResume(StatusRuntimeException.class, e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
