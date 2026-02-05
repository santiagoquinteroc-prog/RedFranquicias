package com.red.franquicias.infrastructure.entrypoint.web.router;

import com.red.franquicias.infrastructure.entrypoint.web.handler.FranchiseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FranchiseRouter {
    @Bean
    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                .POST("/franchises", handler::create)
                .PUT("/franchises/{id}", handler::updateName)
                .build();
    }
}


