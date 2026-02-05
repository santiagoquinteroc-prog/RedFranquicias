package com.red.franquicias.infrastructure.drivenadapter.r2dbc.adapter;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.BranchEntity;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository.BranchRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class BranchRepositoryAdapter implements BranchRepositoryPort {
    private final BranchRepository repository;

    public BranchRepositoryAdapter(BranchRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Branch> save(Branch branch) {
        BranchEntity entity = toEntity(branch);
        return repository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Mono<Branch> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<Branch> findByIdAndFranchiseId(Long id, Long franchiseId) {
        return repository.findByIdAndFranchiseId(id, franchiseId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNameAndFranchiseId(String name, Long franchiseId) {
        return repository.findByNameAndFranchiseId(name, franchiseId)
                .hasElement();
    }

    @Override
    public Flux<Branch> findByFranchiseId(Long franchiseId) {
        return repository.findByFranchiseId(franchiseId)
                .map(this::toDomain);
    }

    private BranchEntity toEntity(Branch branch) {
        return new BranchEntity(branch.getId(), branch.getFranchiseId(), branch.getName());
    }

    private Branch toDomain(BranchEntity entity) {
        return new Branch(entity.getId(), entity.getFranchiseId(), entity.getName());
    }
}

