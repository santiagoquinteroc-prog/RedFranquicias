package com.red.franquicias.infrastructure.drivenadapter.r2dbc.adapter;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.FranchiseEntity;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository.FranchiseRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FranchiseRepositoryAdapter implements FranchiseRepositoryPort {
    private final FranchiseRepository repository;

    public FranchiseRepositoryAdapter(FranchiseRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        FranchiseEntity entity = toEntity(franchise);
        return repository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Mono<Franchise> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return repository.findByName(name)
                .hasElement();
    }

    private FranchiseEntity toEntity(Franchise franchise) {
        return new FranchiseEntity(franchise.getId(), franchise.getName());
    }

    private Franchise toDomain(FranchiseEntity entity) {
        return new Franchise(entity.getId(), entity.getName());
    }
}


