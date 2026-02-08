package com.red.franquicias.infrastructure.config;

import com.red.franquicias.domain.exception.ProcessorException;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@Order(-2)
public class GlobalErrorHandler implements WebExceptionHandler {

    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        HttpStatus status;
        String message;

        if (ex instanceof ConstraintViolationException cve) {
            status = HttpStatus.BAD_REQUEST;
            message = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .findFirst()
                    .orElse("Validation error");

        } else if (ex instanceof ProcessorException pe) {

            status = safeStatus(pe.getTechnicalMessage().getCode(), HttpStatus.INTERNAL_SERVER_ERROR);

            message = pe.getTechnicalMessage().getMessage();

        } else if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : "Request error";


        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = ex.getMessage() != null ? ex.getMessage() : "An error occurred";
        }

        String error = status.getReasonPhrase();

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                exchange.getRequest().getPath().value(),
                status.value(),
                error,
                message
        );

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"path\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                errorResponse.timestamp(),
                errorResponse.path(),
                errorResponse.status(),
                escapeJson(errorResponse.error()),
                escapeJson(errorResponse.message())
        );

        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private HttpStatus safeStatus(String code, HttpStatus fallback) {
        try {
            return HttpStatus.valueOf(Integer.parseInt(code));
        } catch (Exception e) {
            return fallback;
        }
    }

    private String escapeJson(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
