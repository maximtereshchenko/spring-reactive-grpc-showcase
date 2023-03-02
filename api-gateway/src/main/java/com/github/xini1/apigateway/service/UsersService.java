package com.github.xini1.apigateway.service;

import com.github.xini1.apigateway.dto.LoginDto;
import com.github.xini1.apigateway.dto.RegisterUserDto;
import com.github.xini1.apigateway.dto.UserDto;
import com.github.xini1.users.rpc.DecodeJwtRequest;
import com.github.xini1.users.rpc.IdResponse;
import com.github.xini1.users.rpc.JwtResponse;
import com.github.xini1.users.rpc.UserServiceGrpc;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
public final class UsersService {

    private final UserServiceGrpc.UserServiceStub userServiceGrpc;

    public UsersService(String address, int port, ChannelCredentials channelCredentials) {
        userServiceGrpc = UserServiceGrpc.newStub(
                Grpc.newChannelBuilderForAddress(address, port, channelCredentials)
                        .build()
        );
    }

    public Mono<String> register(RegisterUserDto dto) {
        return Mono.create(sink ->
                userServiceGrpc.register(
                        dto.toRegisterRequest(),
                        new MonoStreamObserverAdapter<>(sink, IdResponse::getId)
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
