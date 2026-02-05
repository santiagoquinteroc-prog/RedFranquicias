package com.red.franquicias.infrastructure.entrypoint.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long branchId;
    private String name;
    private Integer stock;
}

