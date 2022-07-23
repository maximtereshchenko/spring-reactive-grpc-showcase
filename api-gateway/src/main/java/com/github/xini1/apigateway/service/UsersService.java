package com.github.xini1.apigateway.service;

import com.github.xini1.apigateway.dto.*;
import com.github.xini1.users.rpc.*;
import io.grpc.*;
import reactor.core.publisher.*;

import java.util.*;

/**
 * @author Maxim Tereshchenko
 */
public final class UsersService {

    private final UserServiceGrpc.UserServiceStub userServiceGrpc;

    public UsersService(String address, int port) {
        userServiceGrpc = UserServiceGrpc.newStub(
                ManagedChannelBuilder.forAddress(address, port)
                        .usePlaintext()
                        .build()
        );
    }

    public Mono<UUID> register(RegisterUserDto dto) {
        return Mono.create(sink ->
                userServiceGrpc.register(
                        dto.toRegisterRequest(),
                        new MonoStreamObserverAdapter<>(sink, response -> UUID.fromString(response.getId()))
                )
        );
    }

    public Mono<String> login(LoginDto dto) {
        return Mono.create(sink ->
                userServiceGrpc.login(
                        dto.toLoginRequest(),
                        new MonoStreamObserverAdapter<>(sink, JwtResponse::getJwt)
                )
        );
    }

    public Mono<UserDto> decode(String jwt) {
        return Mono.create(sink ->
                userServiceGrpc.decode(
                        DecodeJwtRequest.newBuilder()
                                .setJwt(jwt)
                                .build(),
                        new MonoStreamObserverAdapter<>(
                                sink,
                                response -> new UserDto(response.getUserId(), response.getUserType())
                        )
                )
        );
    }
}
