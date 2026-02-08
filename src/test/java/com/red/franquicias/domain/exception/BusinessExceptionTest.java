package com.red.franquicias.domain.exception;

import com.red.franquicias.domain.enums.TechnicalMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_withTechnicalMessage_shouldUseEnumMessage() {
        BusinessException exception =
                new BusinessException(TechnicalMessage.BRANCH_NOT_FOUND);

        assertNotNull(exception);
        assertEquals(
                TechnicalMessage.BRANCH_NOT_FOUND.getMessage(),
                exception.getMessage()
        );
        assertEquals(
                TechnicalMessage.BRANCH_NOT_FOUND,
                exception.getTechnicalMessage()
        );
    }
}
