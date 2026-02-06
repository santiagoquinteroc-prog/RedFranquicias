package com.red.franquicias.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotFoundExceptionTest {
    @Test
    void constructor_withMessage_shouldCreateExceptionWithMessage() {
        String message = "Resource not found";
        NotFoundException exception = new NotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withNullMessage_shouldCreateException() {
        NotFoundException exception = new NotFoundException(null);

        assertNotNull(exception);
    }
}

