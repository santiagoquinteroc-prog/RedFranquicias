package com.red.franquicias.domain.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductValidatorTest {
    @Test
    void validateName_validName_shouldNotThrow() {
        assertDoesNotThrow(() -> ProductValidator.validateName("Valid Product Name"));
    }

    @Test
    void validateName_nullName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateName(null));
    }

    @Test
    void validateName_emptyName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateName(""));
    }

    @Test
    void validateName_blankName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateName("   "));
    }

    @Test
    void validateName_nameTooLong_shouldThrowException() {
        String longName = "a".repeat(61);
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateName(longName));
    }

    @Test
    void validateName_nameExactly60Characters_shouldNotThrow() {
        String name = "a".repeat(60);
        assertDoesNotThrow(() -> ProductValidator.validateName(name));
    }

    @Test
    void validateStock_validStock_shouldNotThrow() {
        assertDoesNotThrow(() -> ProductValidator.validateStock(0));
        assertDoesNotThrow(() -> ProductValidator.validateStock(10));
        assertDoesNotThrow(() -> ProductValidator.validateStock(100));
    }

    @Test
    void validateStock_nullStock_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateStock(null));
    }

    @Test
    void validateStock_negativeStock_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateStock(-1));
        assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateStock(-100));
    }
}

