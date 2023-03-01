package com.github.xini1.common.rpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * @author Maxim Tereshchenko
 */
public final class RpcServer {

    private final Server server;

    public RpcServer(BindableService service) {
        server = ServerBuilder.forPort(8080)
                .addService(service)
                .addService(new HealthStatusManager().getHealthService())
                .build();
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
