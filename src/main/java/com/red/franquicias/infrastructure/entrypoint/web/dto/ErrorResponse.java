package com.red.franquicias.infrastructure.entrypoint.web.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        String path,
        int status,
        String error,
        String message
) {
}
