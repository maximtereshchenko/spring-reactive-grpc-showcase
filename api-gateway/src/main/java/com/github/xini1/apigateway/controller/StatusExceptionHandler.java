package com.github.xini1.apigateway.controller;

import io.grpc.*;
import org.springframework.http.*;
import reactor.core.publisher.*;

/**
 * @author Maxim Tereshchenko
 */
final class StatusExceptionHandler {

    <T> Mono<ResponseEntity<T>> handle(StatusRuntimeException e) {
        return Mono.just(responseEntity(e));
    }

    private <T> ResponseEntity<T> responseEntity(StatusRuntimeException e) {
        switch (e.getStatus().getCode()) {
            case PERMISSION_DENIED:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case NOT_FOUND:
                return ResponseEntity.notFound().build();
            default:
                return ResponseEntity.badRequest().build();
        }
    }
}
