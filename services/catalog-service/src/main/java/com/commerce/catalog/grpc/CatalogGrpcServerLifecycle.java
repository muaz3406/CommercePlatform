package com.commerce.catalog.grpc;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.commerce.catalog.product.CatalogGrpcService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "grpc.server.enabled", havingValue = "true")
public class CatalogGrpcServerLifecycle implements SmartLifecycle {

    private final CatalogGrpcService catalogGrpcService;
    private final Environment environment;
    private final int port;
    private final String certificateChain;
    private final String privateKey;
    private final String clientCaCertificate;
    private volatile Server server;

    public CatalogGrpcServerLifecycle(
            CatalogGrpcService catalogGrpcService,
            Environment environment,
            @Value("${grpc.server.port:9091}") int port,
            @Value("${grpc.server.tls.certificate-chain:}") String certificateChain,
            @Value("${grpc.server.tls.private-key:}") String privateKey,
            @Value("${grpc.server.tls.client-ca-certificate:}") String clientCaCertificate) {
        this.catalogGrpcService = catalogGrpcService;
        this.environment = environment;
        this.port = port;
        this.certificateChain = certificateChain;
        this.privateKey = privateKey;
        this.clientCaCertificate = clientCaCertificate;
    }

    @Override
    public synchronized void start() {
        if (server != null) {
            return;
        }
        try {
            NettyServerBuilder builder = NettyServerBuilder.forPort(port).addService(catalogGrpcService);
            boolean hasMutualTls = !certificateChain.isBlank()
                    && !privateKey.isBlank()
                    && !clientCaCertificate.isBlank();
            if (!hasMutualTls && !environment.acceptsProfiles(Profiles.of("local"))) {
                throw new IllegalStateException(
                        "Server certificate, private key, and client CA certificate are required outside local profile");
            }
            if (hasMutualTls) {
                builder.sslContext(GrpcSslContexts.configure(SslContextBuilder
                                .forServer(new File(certificateChain), new File(privateKey))
                                .trustManager(new File(clientCaCertificate))
                                .clientAuth(ClientAuth.REQUIRE))
                        .build());
            }
            server = builder.build().start();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start catalog gRPC server on port " + port, exception);
        }
    }

    @Override
    public synchronized void stop() {
        if (server == null) {
            return;
        }
        server.shutdown();
        try {
            if (!server.awaitTermination(10, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            server = null;
        }
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
