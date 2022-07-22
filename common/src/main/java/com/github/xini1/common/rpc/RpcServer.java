package com.github.xini1.common.rpc;

import io.grpc.*;

import javax.annotation.*;
import java.io.*;

/**
 * @author Maxim Tereshchenko
 */
public final class RpcServer {

    private final Server server;

    public RpcServer(BindableService service) {
        this.server = ServerBuilder.forPort(8080)
                .addService(service)
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
