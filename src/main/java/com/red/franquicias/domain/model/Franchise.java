package com.red.franquicias.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Franchise {

    private Long id;

    @NotBlank(message = "Franchise name is required")
    @Size(max = 60, message = "Franchise name must not exceed 60 characters")
    private String name;
}
