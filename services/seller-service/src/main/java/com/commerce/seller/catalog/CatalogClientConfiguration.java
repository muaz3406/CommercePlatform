package com.commerce.seller.catalog;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.io.File;
import java.io.IOException;

@Configuration
public class CatalogClientConfiguration {

    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel catalogChannel(
            @Value("${grpc.clients.catalog.address:localhost:9091}") String address,
            @Value("${grpc.clients.catalog.tls.client-certificate:}") String clientCertificate,
            @Value("${grpc.clients.catalog.tls.private-key:}") String privateKey,
            @Value("${grpc.clients.catalog.tls.trusted-ca-certificate:}") String trustedCaCertificate,
            Environment environment) throws IOException {
        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(address);
        if (environment.acceptsProfiles(Profiles.of("local"))) {
            return builder.usePlaintext().build();
        }

        if (clientCertificate.isBlank() || privateKey.isBlank() || trustedCaCertificate.isBlank()) {
            throw new IllegalStateException(
                    "Catalog gRPC client certificate, private key, and trusted CA are required outside local profile");
        }

        return builder.sslContext(GrpcSslContexts.configure(SslContextBuilder.forClient()
                        .trustManager(new File(trustedCaCertificate))
                        .keyManager(new File(clientCertificate), new File(privateKey)))
                .build()).build();
    }
}
