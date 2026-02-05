package com.red.franquicias.infrastructure.entrypoint.web.mapper;

import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.BranchEntity;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.BranchResponse;

public class BranchMapper {
    public static Branch toDomain(BranchRequest request, Long franchiseId) {
        return new Branch(null, franchiseId, request.getName());
    }

    public static BranchResponse toResponse(Branch branch) {
        return new BranchResponse(branch.getId(), branch.getFranchiseId(), branch.getName());
    }

    public static BranchEntity toEntity(Branch branch) {
        return new BranchEntity(branch.getId(), branch.getFranchiseId(), branch.getName());
    }

    public static Branch toDomain(BranchEntity entity) {
        return new Branch(entity.getId(), entity.getFranchiseId(), entity.getName());
    }
}

