package com.commerce.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest
class ApiGatewayRoutesTests {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void routesPublicApiPathsToOwningServices() {
        Map<String, String> routes = routeLocator.getRoutes()
                .collectMap(route -> route.getId(), route -> route.getUri().toString())
                .block();

        assertThat(routes)
                .containsEntry("catalog-service", "http://localhost:8081")
                .containsEntry("seller-service", "http://localhost:8082");
    }
}
