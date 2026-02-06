package com.red.franquicias.domain.validator;

public class FranchiseValidator {
    private static final int MAX_NAME_LENGTH = 60;

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Franchise name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Franchise name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }
}


