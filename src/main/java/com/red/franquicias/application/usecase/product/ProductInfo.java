package com.red.franquicias.application.usecase.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfo {
    private Long productId;
    private String name;
    private Integer stock;
}

