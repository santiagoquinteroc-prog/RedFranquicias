package com.red.franquicias.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {
    FRANCHISE_NAME_ALREADY_EXISTS("409", "Franchise name already exists", "name"),
    FRANCHISE_CREATE_ERROR("500", "Error creating franchise", null),
    FRANCHISE_UPDATE_NAME_ERROR("500", "Error updating franchise name", null),
    FRANCHISE_NOT_FOUND("404", "Franchise not found", "franchiseId"),
    BRANCH_NAME_ALREADY_EXISTS("409", "Branch name already exists in this franchise", "name"),
    BRANCH_UPDATE_ERROR("500", "Error updating branch name", null),
    BRANCH_CREATE_ERROR("500", "Error creating branch", null),
    BRANCH_NOT_FOUND("404", "Branch not found or does not belong to franchise", "branchId"),
    PRODUCT_NOT_FOUND("404", "Product not found", "productId"),
    PRODUCT_NAME_ALREADY_EXISTS("409", "Product name already exists in this branch", "name"),
    TOP_PRODUCTS_QUERY_ERROR("500", "Error retrieving top products", null);

    private final String code;
    private final String message;
    private final String param;
}