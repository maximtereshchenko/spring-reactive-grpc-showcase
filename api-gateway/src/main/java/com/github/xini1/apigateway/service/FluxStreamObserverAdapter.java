package com.github.xini1.apigateway.service;

import io.grpc.stub.StreamObserver;
import reactor.core.publisher.FluxSink;

import java.util.function.Function;

/**
 * @author Maxim Tereshchenko
 */
final class FluxStreamObserverAdapter<T, R> implements StreamObserver<T> {

    private final FluxSink<R> sink;
    private final Function<T, R> mapper;

    public FluxStreamObserverAdapter(FluxSink<R> sink, Function<T, R> mapper) {
        this.sink = sink;
        this.mapper = mapper;
    }

    @Override
    public void onNext(T value) {
        sink.next(mapper.apply(value));
    }

    @Override
    public void onError(Throwable t) {
        sink.error(t);
    }

    @Override
    public void onCompleted() {
        sink.complete();
    }
}
