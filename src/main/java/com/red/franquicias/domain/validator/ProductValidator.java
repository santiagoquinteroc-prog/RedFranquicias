package com.red.franquicias.domain.validator;

public class ProductValidator {
    private static final int MAX_NAME_LENGTH = 60;

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Product name must not exceed " + MAX_NAME_LENGTH + " characters");
        }
    }

    public static void validateStock(Integer stock) {
        if (stock == null) {
            throw new IllegalArgumentException("Product stock is required");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Product stock must be greater than or equal to 0");
        }
    }
}

