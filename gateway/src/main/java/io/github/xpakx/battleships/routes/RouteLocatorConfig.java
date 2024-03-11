package io.github.xpakx.battleships.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.DedupeResponseHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class RouteLocatorConfig {
    @Bean
    public RouteLocator myRoutes(
            RouteLocatorBuilder builder,
            GatewayFilter dedupeResponseHeaderFilter,
            @Value("${main.url}") final String mainUrl,
            @Value("${game.url}") final String gameUrl
            ) {
        return builder.routes()
                .route("main", r -> r
                        .path("/authenticate", "/register", "/game/**")
                        .filters(f -> f.filter(dedupeResponseHeaderFilter))
                        .uri(mainUrl))
                .route("game", r -> r
                        .path("/app/**", "/topic/**", "/play/**")
                        .filters(f -> f.filter(dedupeResponseHeaderFilter))
                        .uri(gameUrl))
                .build();
    }

    @Bean
    public GatewayFilter dedupeResponseHeaderFilter() {
        return new DedupeResponseHeaderGatewayFilterFactory()
                .apply(config -> {
                    config
                            .setName(
                                    HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS + ", " + HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                            );
                    config
                            .setStrategy(DedupeResponseHeaderGatewayFilterFactory.Strategy.RETAIN_UNIQUE);
                        }
                );
    }
}
