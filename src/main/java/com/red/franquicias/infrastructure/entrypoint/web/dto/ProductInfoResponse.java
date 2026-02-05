package com.red.franquicias.infrastructure.entrypoint.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoResponse {
    private Long productId;
    private String name;
    private Integer stock;
}

