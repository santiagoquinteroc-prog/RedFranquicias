package com.red.franquicias.infrastructure.config;

import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@org.springframework.stereotype.Component
@Order(-2)
public class GlobalErrorHandler implements WebExceptionHandler {
    private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String error = "Internal Server Error";
        String message = ex.getMessage();

        if (ex instanceof ConstraintViolationException) {
            status = HttpStatus.BAD_REQUEST;
            error = "Bad Request";
            ConstraintViolationException cve = (ConstraintViolationException) ex;
            message = cve.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .findFirst()
                    .orElse("Validation error");
        } else if (ex instanceof ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            error = "Bad Request";
        } else if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            error = "Not Found";
        } else if (ex instanceof ConflictException) {
            status = HttpStatus.CONFLICT;
            error = "Conflict";
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            error = rse.getReason() != null ? rse.getReason() : error;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                exchange.getRequest().getPath().value(),
                status.value(),
                error,
                message != null ? message : "An error occurred"
        );

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"path\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                errorResponse.timestamp(),
                errorResponse.path(),
                errorResponse.status(),
                errorResponse.error(),
                errorResponse.message()
        );

        DataBuffer buffer = bufferFactory.wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}