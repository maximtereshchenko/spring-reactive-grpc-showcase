package com.github.xini1.common.rpc;

import com.github.xini1.common.Shared;
import io.grpc.*;
import io.grpc.protobuf.services.HealthStatusManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * @author Maxim Tereshchenko
 */
public final class RpcServer {

    private final Server server;
    private final Server healthCheck;

    public RpcServer(BindableService service) throws IOException {
        try (var certificate = Shared.serverCertificate(); var privateKey = Shared.serverPrivateKey()) {
            server = Grpc.newServerBuilderForPort(
                            Shared.PORT,
                            TlsServerCredentials.newBuilder()
                                    .keyManager(certificate, privateKey)
                                    .build()
                    )
                    .addService(service)
                    .build();
        }
        healthCheck = ServerBuilder.forPort(Shared.HEALTH_CHECK_PORT)
                .addService(new HealthStatusManager().getHealthService())
                .build();
    }

    @PostConstruct
    void start() throws IOException {
        server.start();
        healthCheck.start();
    }

    @PreDestroy
    void stop() throws InterruptedException {
        server.shutdown();
        healthCheck.shutdown();
        server.awaitTermination();
        healthCheck.awaitTermination();
    }
}
