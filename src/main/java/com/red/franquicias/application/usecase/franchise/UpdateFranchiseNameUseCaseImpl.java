package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Franchise;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Service
@Validated
public class UpdateFranchiseNameUseCaseImpl implements UpdateFranchiseNameUseCase {

    private final FranchiseRepositoryPort repositoryPort;

    public UpdateFranchiseNameUseCaseImpl(FranchiseRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Mono<Franchise> updateName(Long id,
                                      String name
    ) {
        return repositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(existing -> repositoryPort.existsByName(name)
                        .flatMap(exists -> {
                            if (exists && !existing.getName().equals(name)) {
                                return Mono.error(new ConflictException("Franchise name already exists"));
                            }
                            existing.setName(name);
                            return repositoryPort.save(existing);
                        }));
    }
}
