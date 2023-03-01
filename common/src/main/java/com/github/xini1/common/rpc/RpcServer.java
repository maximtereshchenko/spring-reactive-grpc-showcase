package com.github.xini1.common.rpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Maxim Tereshchenko
 */
public final class RpcServer {

    private final Server server;

    public RpcServer(BindableService service) {
        server = ServerBuilder.forPort(8080)
                .addService(service)
                .build();
    }

    public void awaitTermination() throws InterruptedException {
        server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    @PostConstruct
    void start() throws IOException {
        server.start();
    }

    @PreDestroy
    void stop() throws InterruptedException {
        server.shutdown();
        server.awaitTermination();
    }
}
