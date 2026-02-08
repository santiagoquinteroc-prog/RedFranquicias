package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.exception.TechnicalException;
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
                        return Mono.error(new BusinessException(
                                TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS
                        ));
                    }
                    return repositoryPort.save(franchise);
                })
                .onErrorMap(
                        ex -> !(ex instanceof BusinessException),
                        ex -> new TechnicalException(ex, TechnicalMessage.FRANCHISE_CREATE_ERROR)
                );
    }
}
