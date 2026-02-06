package com.red.franquicias.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConflictExceptionTest {
    @Test
    void constructor_withMessage_shouldCreateExceptionWithMessage() {
        String message = "Conflict error message";
        ConflictException exception = new ConflictException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withNullMessage_shouldCreateException() {
        ConflictException exception = new ConflictException(null);

        assertNotNull(exception);
    }
}

