package com.red.franquicias.application.usecase.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchTopProduct {
    private Long branchId;
    private String branchName;
    private ProductInfo product;
}

