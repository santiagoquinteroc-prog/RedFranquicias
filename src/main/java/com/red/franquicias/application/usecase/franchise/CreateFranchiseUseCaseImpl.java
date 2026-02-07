package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.model.Franchise;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Service
@Validated
public class CreateFranchiseUseCaseImpl implements CreateFranchiseUseCase {

    private final FranchiseRepositoryPort repositoryPort;

    public CreateFranchiseUseCaseImpl(FranchiseRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Mono<Franchise> create(Franchise franchise) {
        return repositoryPort.existsByName(franchise.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Franchise name already exists"));
                    }
                    return repositoryPort.save(franchise);
                });
    }
}
