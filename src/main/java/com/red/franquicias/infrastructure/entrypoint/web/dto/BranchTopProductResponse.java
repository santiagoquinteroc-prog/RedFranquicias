package com.red.franquicias.infrastructure.entrypoint.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchTopProductResponse {
    private Long branchId;
    private String branchName;
    private ProductInfoResponse product;
}

