package com.red.franquicias.domain.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FranchiseValidatorTest {
    @Test
    void validateName_validName_shouldNotThrow() {
        assertDoesNotThrow(() -> FranchiseValidator.validateName("Valid Franchise Name"));
    }

    @Test
    void validateName_nullName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> FranchiseValidator.validateName(null));
    }

    @Test
    void validateName_emptyName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> FranchiseValidator.validateName(""));
    }

    @Test
    void validateName_blankName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> FranchiseValidator.validateName("   "));
    }

    @Test
    void validateName_nameTooLong_shouldThrowException() {
        String longName = "a".repeat(61);
        assertThrows(IllegalArgumentException.class, () -> FranchiseValidator.validateName(longName));
    }

    @Test
    void validateName_nameExactly60Characters_shouldNotThrow() {
        String name = "a".repeat(60);
        assertDoesNotThrow(() -> FranchiseValidator.validateName(name));
    }
}


