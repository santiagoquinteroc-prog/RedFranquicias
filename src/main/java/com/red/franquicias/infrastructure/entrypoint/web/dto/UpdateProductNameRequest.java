package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductNameRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 60, message = "Product name must not exceed 60 characters")
    private String name;
}

