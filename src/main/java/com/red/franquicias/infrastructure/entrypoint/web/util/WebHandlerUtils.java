package com.red.franquicias.infrastructure.entrypoint.web.util;

import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.infrastructure.entrypoint.web.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;

public final class WebHandlerUtils {

    private WebHandlerUtils() {}

    public static Mono<Long> parseLongPathVariable(ServerRequest request, String name) {
        try {
            return Mono.just(Long.parseLong(request.pathVariable(name)));
        } catch (NumberFormatException ex) {
            return Mono.error(new IllegalArgumentException(name + " must be a valid number"));
        } catch (IllegalArgumentException ex) {
            return Mono.error(new IllegalArgumentException(name + " is required"));
        }
    }

    public static <T> void validate(Validator validator, T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .findFirst()
                    .orElse("Validation error");
            throw new IllegalArgumentException(message);
        }
    }

    public static HttpStatus mapBusinessStatus(TechnicalMessage msg) {
        if (msg == null) return HttpStatus.BAD_REQUEST;
        return switch (msg) {
            case FRANCHISE_NOT_FOUND, BRANCH_NOT_FOUND, PRODUCT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FRANCHISE_NAME_ALREADY_EXISTS, BRANCH_NAME_ALREADY_EXISTS, PRODUCT_NAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    public static Mono<ServerResponse> error(
            ServerRequest request,
            HttpStatus status,
            String message
    ) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                request.path(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(error);
    }

    public static Mono<ServerResponse> error(
            ServerRequest request,
            HttpStatus status,
            TechnicalMessage technicalMessage
    ) {
        String message = technicalMessage != null
                ? technicalMessage.getMessage()
                : "Error";

        return error(request, status, message);
    }
}

