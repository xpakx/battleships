package io.github.xpakx.battleships.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class RouteLocatorConfig {
    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("main", r -> r
                        .path("/authenticate", "/register", "/game/**")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri("http://localhost:8080"))
                .route("game", r -> r
                        .path("/app/**", "/topic/**", "/play/**")
                        .uri("http://localhost:8081"))
                .build();
    }
}
