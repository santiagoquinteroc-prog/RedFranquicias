package com.red.franquicias.infrastructure.entrypoint.web.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class HealthHandler {
    public Mono<ServerResponse> health(ServerRequest request) {
        Map<String, String> response = Map.of("status", "ok");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }
}

