package com.red.franquicias.infrastructure.entrypoint.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {
    @NotBlank(message = "Branch name is required")
    @Size(max = 60, message = "Branch name must not exceed 60 characters")
    private String name;
}

