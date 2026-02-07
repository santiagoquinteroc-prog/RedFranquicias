package com.red.franquicias.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    private Long id;

    @NotNull(message = "Franchise id is required")
    private Long franchiseId;

    @NotBlank(message = "Branch name is required")
    @Size(max = 60, message = "Branch name must not exceed 60 characters")
    private String name;
}
