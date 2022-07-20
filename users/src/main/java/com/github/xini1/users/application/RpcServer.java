package com.github.xini1.users.application;

import com.github.xini1.users.domain.Module;
import io.grpc.*;

import javax.annotation.*;
import java.io.*;

/**
 * @author Maxim Tereshchenko
 */
final class RpcServer {

    private final Server server;

    RpcServer(Module module) {
        this.server = ServerBuilder.forPort(8080)
                .addService(
                        new UserRpcService(module.registerUseCase(), module.loginUseCase(), module.decodeJwtUseCase())
                )
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
