package com.red.franquicias.infrastructure.entrypoint.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResponse {
    private Long franchiseId;
    private String franchiseName;
    private List<BranchTopProductResponse> results;
}

