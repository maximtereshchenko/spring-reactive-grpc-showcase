package com.github.xini1.apigateway.service;

import io.grpc.stub.*;
import reactor.core.publisher.*;

/**
 * @author Maxim Tereshchenko
 */
final class VoidMonoStreamObserverAdapter<T> implements StreamObserver<T> {

    private final MonoStreamObserverAdapter<T, Void> original;

    VoidMonoStreamObserverAdapter(MonoSink<Void> sink) {
        original = new MonoStreamObserverAdapter<>(sink, ignored -> null);
    }

    @Override
    public void onNext(T value) {
        original.onNext(value);
    }

    @Override
    public void onError(Throwable t) {
        original.onError(t);
    }

    @Override
    public void onCompleted() {
        original.onCompleted();
    }
}
