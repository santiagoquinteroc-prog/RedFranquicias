package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    @Size(max = 60, message = "Product name must not exceed 60 characters")
    private String name;

    @NotNull(message = "Product stock is required")
    @Min(value = 0, message = "Product stock must be greater than or equal to 0")
    private Integer stock;
}

