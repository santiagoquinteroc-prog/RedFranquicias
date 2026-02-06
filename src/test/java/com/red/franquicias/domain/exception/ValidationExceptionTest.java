package com.red.franquicias.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidationExceptionTest {
    @Test
    void constructor_withMessage_shouldCreateExceptionWithMessage() {
        String message = "Validation error message";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withNullMessage_shouldCreateException() {
        ValidationException exception = new ValidationException(null);

        assertNotNull(exception);
    }
}

