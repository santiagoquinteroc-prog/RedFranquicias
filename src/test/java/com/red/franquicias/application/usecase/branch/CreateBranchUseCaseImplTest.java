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
class CreateBranchUseCaseImplTest {
    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private CreateBranchUseCaseImpl useCase;

    private Franchise existingFranchise;
    private Branch validBranch;

    @BeforeEach
    void setUp() {
        existingFranchise = new Franchise(1L, "Test Franchise");
        validBranch = new Branch(null, 1L, "Test Branch");
    }

    @Test
    void create_validBranch_shouldReturnCreatedBranch() {
        Branch savedBranch = new Branch(1L, 1L, "Test Branch");
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Test Branch", 1L)).thenReturn(Mono.just(false));
        when(branchRepositoryPort.save(any(Branch.class))).thenReturn(Mono.just(savedBranch));

        StepVerifier.create(useCase.create(validBranch))
                .expectNext(savedBranch)
                .verifyComplete();
    }

    @Test
    void create_emptyName_shouldReturnValidationException() {
        Branch branch = new Branch(null, 1L, "");

        StepVerifier.create(useCase.create(branch))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_nullName_shouldReturnValidationException() {
        Branch branch = new Branch(null, 1L, null);

        StepVerifier.create(useCase.create(branch))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_nameTooLong_shouldReturnValidationException() {
        String longName = "a".repeat(61);
        Branch branch = new Branch(null, 1L, longName);

        StepVerifier.create(useCase.create(branch))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("exceed"))
                .verify();
    }

    @Test
    void create_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        Branch branch = new Branch(null, 999L, "Test Branch");
        StepVerifier.create(useCase.create(branch))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void create_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Test Branch", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.create(validBranch))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException
                        && throwable.getMessage().contains("already exists"))
                .verify();
    }
}

