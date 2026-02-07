package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UpdateBranchNameUseCaseImplValidationTest.Config.class)
class UpdateBranchNameUseCaseImplValidationTest {

    @Configuration
    static class Config {

        @Bean
        BranchRepositoryPort branchRepositoryPort() {
            return Mockito.mock(BranchRepositoryPort.class);
        }

        @Bean
        FranchiseRepositoryPort franchiseRepositoryPort() {
            return Mockito.mock(FranchiseRepositoryPort.class);
        }

        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }

        @Bean
        MethodValidationPostProcessor methodValidationPostProcessor(LocalValidatorFactoryBean validator) {
            MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
            processor.setValidator(validator);
            return processor;
        }

        @Bean
        UpdateBranchNameUseCase updateBranchNameUseCase(
                BranchRepositoryPort branchRepositoryPort,
                FranchiseRepositoryPort franchiseRepositoryPort
        ) {
            return new UpdateBranchNameUseCaseImpl(branchRepositoryPort, franchiseRepositoryPort);
        }
    }

    @Autowired
    private UpdateBranchNameUseCase useCase;

    @Autowired
    private BranchRepositoryPort branchRepositoryPort;

    @Autowired
    private FranchiseRepositoryPort franchiseRepositoryPort;

    private Franchise existingFranchise;
    private Branch existingBranch;

    @BeforeEach
    void setUp() {
        Mockito.reset(branchRepositoryPort, franchiseRepositoryPort);
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

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, "New Name")))
                .expectNext(updatedBranch)
                .verifyComplete();
    }

    @Test
    void updateName_emptyName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, "")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, null)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nameTooLong_shouldThrowConstraintViolationException() {
        String longName = "a".repeat(61);

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, longName)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullBranchId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(null, 1L, "New Name")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullFranchiseId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, null, "New Name")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 999L, "New Name")))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateName_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(999L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(999L, 1L, "New Name")))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateName_branchNotBelongsToFranchise_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, "New Name")))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateName_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Duplicate Name", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, "Duplicate Name")))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void updateName_sameName_shouldReturnUpdatedBranch() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Original Name", 1L)).thenReturn(Mono.just(true));
        when(branchRepositoryPort.save(any(Branch.class))).thenReturn(Mono.just(existingBranch));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, "Original Name")))
                .expectNext(existingBranch)
                .verifyComplete();
    }
}