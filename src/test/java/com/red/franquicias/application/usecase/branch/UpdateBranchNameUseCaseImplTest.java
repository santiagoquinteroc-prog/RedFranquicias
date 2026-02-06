package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Branch;
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
class UpdateBranchNameUseCaseImplTest {
    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private UpdateBranchNameUseCaseImpl useCase;

    private Franchise existingFranchise;
    private Branch existingBranch;

    @BeforeEach
    void setUp() {
        existingFranchise = new Franchise(1L, "Test Franchise");
        existingBranch = new Branch(1L, 1L, "Original Name");
    }

    @Test
    void updateName_validName_shouldReturnUpdatedBranch() {
        Branch updatedBranch = new Branch(1L, 1L, "New Name");
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(branchRepositoryPort.existsByNameAndFranchiseId("New Name", 1L)).thenReturn(Mono.just(false));
        when(branchRepositoryPort.save(any(Branch.class))).thenReturn(Mono.just(updatedBranch));

        StepVerifier.create(useCase.updateName(1L, 1L, "New Name"))
                .expectNext(updatedBranch)
                .verifyComplete();
    }

    @Test
    void updateName_emptyName_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateName(1L, 1L, ""))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateName_nullName_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateName(1L, 1L, null))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateName_nameTooLong_shouldReturnValidationException() {
        String longName = "a".repeat(61);
        StepVerifier.create(useCase.updateName(1L, 1L, longName))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("exceed"))
                .verify();
    }

    @Test
    void updateName_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(1L, 999L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(999L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(999L, 1L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_branchNotBelongsToFranchise_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(1L, 1L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Duplicate Name", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.updateName(1L, 1L, "Duplicate Name"))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException
                        && throwable.getMessage().contains("already exists"))
                .verify();
    }

    @Test
    void updateName_sameName_shouldReturnUpdatedBranch() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Original Name", 1L)).thenReturn(Mono.just(true));
        when(branchRepositoryPort.save(any(Branch.class))).thenReturn(Mono.just(existingBranch));

        StepVerifier.create(useCase.updateName(1L, 1L, "Original Name"))
                .expectNext(existingBranch)
                .verifyComplete();
    }
}

