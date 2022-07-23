package com.github.xini1.apigateway.service;

import io.grpc.stub.*;
import reactor.core.publisher.*;

import java.util.function.*;

/**
 * @author Maxim Tereshchenko
 */
final class MonoStreamObserverAdapter<T, R> implements StreamObserver<T> {

    private final MonoSink<R> sink;
    private final Function<T, R> mapper;

    MonoStreamObserverAdapter(MonoSink<R> sink, Function<T, R> mapper) {
        this.sink = sink;
        this.mapper = mapper;
    }

    @Override
    public void onNext(T value) {
        sink.success(mapper.apply(value));
    }

    @Override
    public void onError(Throwable t) {
        sink.error(t);
    }

    @Override
    public void onCompleted() {
        //empty
    }
}
