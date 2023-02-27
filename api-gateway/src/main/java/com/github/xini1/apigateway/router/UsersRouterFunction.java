package com.github.xini1.apigateway.router;

import com.github.xini1.apigateway.dto.LoginDto;
import com.github.xini1.apigateway.dto.RegisterUserDto;
import com.github.xini1.apigateway.service.UsersService;
import io.grpc.StatusRuntimeException;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
public final class UsersRouterFunction implements RouterFunction<ServerResponse> {

    private final RouterFunction<ServerResponse> original;

    public UsersRouterFunction(UsersService usersService) {
        original = RouterFunctions.route()
                .POST("/users", request -> register(usersService, request.bodyToMono(RegisterUserDto.class)))
                .POST("/users/login", request -> login(usersService, request.bodyToMono(LoginDto.class)))
                .filter((request, handler) ->
                        handler.handle(request)
                                .onErrorResume(
                                        StatusRuntimeException.class,
                                        e -> new StatusExceptionHandler().handle(e)
                                )
                )
                .build();
    }

    @Override
    public Mono<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return original.route(request);
    }

    private Mono<ServerResponse> login(UsersService usersService, Mono<LoginDto> dtoMono) {
        return dtoMono.flatMap(usersService::login)
                .flatMap(jwt -> ServerResponse.ok().bodyValue(jwt));
    }

    private Mono<ServerResponse> register(UsersService usersService, Mono<RegisterUserDto> dtoMono) {
        return dtoMono.flatMap(usersService::register)
                .flatMap(id -> ServerResponse.ok().bodyValue(id));
    }
}
