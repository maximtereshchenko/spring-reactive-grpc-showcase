package com.github.xini1.apigateway.router;

import io.grpc.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.*;

/**
 * @author Maxim Tereshchenko
 */
final class StatusExceptionHandler {

    Mono<ServerResponse> handle(StatusRuntimeException e) {
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
