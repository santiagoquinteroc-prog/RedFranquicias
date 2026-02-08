package com.red.franquicias.application.usecase.branch;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
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
@ContextConfiguration(classes = CreateBranchUseCaseImplValidationTest.Config.class)
class CreateBranchUseCaseImplValidationTest {

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
        CreateBranchUseCase createBranchUseCase(
                BranchRepositoryPort branchRepositoryPort,
                FranchiseRepositoryPort franchiseRepositoryPort
        ) {
            return new CreateBranchUseCaseImpl(branchRepositoryPort, franchiseRepositoryPort);
        }
    }

    @Autowired
    private CreateBranchUseCase useCase;

    @Autowired
    private BranchRepositoryPort branchRepositoryPort;

    @Autowired
    private FranchiseRepositoryPort franchiseRepositoryPort;

    private Franchise existingFranchise;
    private Branch validBranch;

    @BeforeEach
    void setUp() {
        Mockito.reset(branchRepositoryPort, franchiseRepositoryPort);
        existingFranchise = new Franchise(1L, "Test Franchise");
        validBranch = new Branch(null, 1L, "Test Branch");
    }

    @Test
    void create_validBranch_shouldReturnCreatedBranch() {
        Branch savedBranch = new Branch(1L, 1L, "Test Branch");
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Test Branch", 1L)).thenReturn(Mono.just(false));
        when(branchRepositoryPort.save(any(Branch.class))).thenReturn(Mono.just(savedBranch));

        StepVerifier.create(Mono.defer(() -> useCase.create(validBranch)))
                .expectNext(savedBranch)
                .verifyComplete();
    }

    @Test
    void create_emptyName_shouldThrowConstraintViolationException() {
        Branch branch = new Branch(null, 1L, "");

        StepVerifier.create(Mono.defer(() -> useCase.create(branch)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullName_shouldThrowConstraintViolationException() {
        Branch branch = new Branch(null, 1L, null);

        StepVerifier.create(Mono.defer(() -> useCase.create(branch)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nameTooLong_shouldThrowConstraintViolationException() {
        String longName = "a".repeat(61);
        Branch branch = new Branch(null, 1L, longName);

        StepVerifier.create(Mono.defer(() -> useCase.create(branch)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullBranch_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.create(null)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullFranchiseId_shouldThrowConstraintViolationException() {
        Branch branch = new Branch(null, null, "Test Branch");

        StepVerifier.create(Mono.defer(() -> useCase.create(branch)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        Branch branch = new Branch(null, 999L, "Test Branch");

        StepVerifier.create(Mono.defer(() -> useCase.create(branch)))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.FRANCHISE_NOT_FOUND
                )
                .verify();
    }

    @Test
    void create_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.existsByNameAndFranchiseId("Test Branch", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(Mono.defer(() -> useCase.create(validBranch)))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS
                )
                .verify();
    }
}