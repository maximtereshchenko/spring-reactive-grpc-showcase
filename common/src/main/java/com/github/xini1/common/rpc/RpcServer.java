package com.github.xini1.common.rpc;

import com.github.xini1.common.Shared;
import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.TlsServerCredentials;
import io.grpc.protobuf.services.HealthStatusManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * @author Maxim Tereshchenko
 */
public final class RpcServer {

    private final Server server;

    public RpcServer(BindableService service) throws IOException {
        try (var certificate = Shared.serverCertificate(); var privateKey = Shared.serverPrivateKey()) {
            server = Grpc.newServerBuilderForPort(
                            8080,
                            TlsServerCredentials.newBuilder()
                                    .keyManager(certificate, privateKey)
                                    .build()
                    )
                    .addService(service)
                    .addService(new HealthStatusManager().getHealthService())
                    .build();
        }
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
