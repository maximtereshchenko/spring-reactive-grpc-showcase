package com.github.xini1.apigateway.router;

import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author Maxim Tereshchenko
 */
final class StatusExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusExceptionHandler.class);

    Mono<ServerResponse> handle(ServerRequest request, StatusRuntimeException e) {
        LOGGER.warn("Could not execute request {}", request, e);
        switch (e.getStatus().getCode()) {
            case PERMISSION_DENIED:
                return ServerResponse.status(HttpStatus.FORBIDDEN).build();
            case NOT_FOUND:
                return ServerResponse.notFound().build();
            default:
                return ServerResponse.badRequest().build();
        }
    }
}
