package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFranchiseNameUseCaseImplTest {
    @Mock
    private FranchiseRepositoryPort repositoryPort;

    @InjectMocks
    private UpdateFranchiseNameUseCaseImpl useCase;

    private Franchise existingFranchise;

    @BeforeEach
    void setUp() {
        existingFranchise = new Franchise(1L, "Original Name");
    }

    @Test
    void updateName_validName_shouldReturnUpdatedFranchise() {
        Franchise updatedFranchise = new Franchise(1L, "New Name");
        when(repositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(repositoryPort.existsByName("New Name")).thenReturn(Mono.just(false));
        when(repositoryPort.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));

        StepVerifier.create(useCase.updateName(1L, "New Name"))
                .expectNext(updatedFranchise)
                .verifyComplete();
    }

    @Test
    void updateName_emptyName_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateName(1L, ""))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateName_nullName_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateName(1L, null))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateName_nameTooLong_shouldReturnValidationException() {
        String longName = "a".repeat(61);
        StepVerifier.create(useCase.updateName(1L, longName))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("exceed"))
                .verify();
    }

    @Test
    void updateName_franchiseNotFound_shouldReturnNotFoundException() {
        when(repositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(999L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_duplicateName_shouldReturnConflictException() {
        when(repositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(repositoryPort.existsByName("Duplicate Name")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.updateName(1L, "Duplicate Name"))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException
                        && throwable.getMessage().contains("already exists"))
                .verify();
    }

    @Test
    void updateName_sameName_shouldReturnUpdatedFranchise() {
        when(repositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(repositoryPort.existsByName("Original Name")).thenReturn(Mono.just(true));
        when(repositoryPort.save(any(Franchise.class))).thenReturn(Mono.just(existingFranchise));

        StepVerifier.create(useCase.updateName(1L, "Original Name"))
                .expectNext(existingFranchise)
                .verifyComplete();
    }
}

