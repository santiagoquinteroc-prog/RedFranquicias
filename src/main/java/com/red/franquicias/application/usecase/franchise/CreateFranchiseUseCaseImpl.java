package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.domain.validator.FranchiseValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateFranchiseUseCaseImpl implements CreateFranchiseUseCase {
    private final FranchiseRepositoryPort repositoryPort;

    public CreateFranchiseUseCaseImpl(FranchiseRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Mono<Franchise> create(Franchise franchise) {
        try {
            FranchiseValidator.validateName(franchise.getName());
        } catch (IllegalArgumentException e) {
            return Mono.error(new ValidationException(e.getMessage()));
        }

        return repositoryPort.existsByName(franchise.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Franchise name already exists"));
                    }
                    return repositoryPort.save(franchise);
                });
    }
}


