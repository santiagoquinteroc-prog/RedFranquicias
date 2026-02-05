package com.red.franquicias.application.usecase.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResult {
    private Long franchiseId;
    private String franchiseName;
    private List<BranchTopProduct> results;
}

