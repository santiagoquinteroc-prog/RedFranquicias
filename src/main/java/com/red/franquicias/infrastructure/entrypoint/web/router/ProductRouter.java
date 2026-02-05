package com.red.franquicias.infrastructure.entrypoint.web.router;

import com.red.franquicias.infrastructure.entrypoint.web.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductRouter {
    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler handler) {
        return RouterFunctions.route()
                .POST("/franchises/{franchiseId}/branches/{branchId}/products", handler::create)
                .PUT("/franchises/{franchiseId}/branches/{branchId}/products/{productId}", handler::updateName)
                .PATCH("/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock", handler::updateStock)
                .build();
    }
}

