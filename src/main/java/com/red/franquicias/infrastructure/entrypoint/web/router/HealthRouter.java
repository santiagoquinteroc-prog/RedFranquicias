package com.red.franquicias.infrastructure.entrypoint.web.router;

import com.red.franquicias.infrastructure.entrypoint.web.handler.HealthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class HealthRouter {
    @Bean
    public RouterFunction<ServerResponse> healthRoutes(HealthHandler handler) {
        return RouterFunctions.route()
                .GET("/health", handler::health)
                .build();
    }
}


