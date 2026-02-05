package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FranchiseRequest {
    @NotBlank(message = "Franchise name is required")
    @Size(max = 60, message = "Franchise name must not exceed 60 characters")
    private String name;

    public FranchiseRequest() {
    }

    public FranchiseRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


