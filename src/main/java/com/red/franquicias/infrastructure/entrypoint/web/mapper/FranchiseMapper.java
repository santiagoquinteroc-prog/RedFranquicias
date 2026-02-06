package com.red.franquicias.infrastructure.entrypoint.web.mapper;

import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.FranchiseEntity;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseRequest;
import com.red.franquicias.infrastructure.entrypoint.web.dto.FranchiseResponse;

public class FranchiseMapper {
    public static Franchise toDomain(FranchiseRequest request) {
        return new Franchise(null, request.name());
    }

    public static FranchiseResponse toResponse(Franchise franchise) {
        return new FranchiseResponse(franchise.getId(), franchise.getName());
    }

    public static FranchiseEntity toEntity(Franchise franchise) {
        return new FranchiseEntity(franchise.getId(), franchise.getName());
    }

    public static Franchise toDomain(FranchiseEntity entity) {
        return new Franchise(entity.getId(), entity.getName());
    }
}



