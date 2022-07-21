package com.github.xini1.orders.write.application;

import com.github.xini1.orders.write.domain.Module;
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
                        new OrderRpcService(
                                module.activateItemUseCase(),
                                module.addItemToCartUseCase(),
                                module.createItemUseCase(),
                                module.deactivateItemUseCase(),
                                module.orderItemsInCartUseCase(),
                                module.removeItemFromCartUseCase()
                        )
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
