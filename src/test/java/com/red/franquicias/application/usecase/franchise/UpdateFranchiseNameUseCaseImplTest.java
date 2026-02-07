package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
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
@ContextConfiguration(classes = UpdateFranchiseNameUseCaseImplValidationTest.Config.class)
class UpdateFranchiseNameUseCaseImplValidationTest {

    @Configuration
    static class Config {

        @Bean
        FranchiseRepositoryPort repositoryPort() {
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
        UpdateFranchiseNameUseCase updateFranchiseNameUseCase(FranchiseRepositoryPort repositoryPort) {
            return new UpdateFranchiseNameUseCaseImpl(repositoryPort);
        }
    }

    @Autowired
    private UpdateFranchiseNameUseCase useCase;

    @Autowired
    private FranchiseRepositoryPort repositoryPort;

    private Franchise existingFranchise;

    @BeforeEach
    void setUp() {
        Mockito.reset(repositoryPort);
        existingFranchise = new Franchise(1L, "Original Name");
    }

    @Test
    void updateName_validName_shouldReturnUpdatedFranchise() {
        Franchise updatedFranchise = new Franchise(1L, "New Name");
        when(repositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(repositoryPort.existsByName("New Name")).thenReturn(Mono.just(false));
        when(repositoryPort.save(any(Franchise.class))).thenReturn(Mono.just(updatedFranchise));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, "New Name")))
                .expectNext(updatedFranchise)
                .verifyComplete();
    }

    @Test
    void updateName_emptyName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, "")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, null)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nameTooLong_shouldThrowConstraintViolationException() {
        String longName = "a".repeat(61);

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, longName)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(null, "New Name")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_franchiseNotFound_shouldReturnNotFoundException() {
        when(repositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(999L, "New Name")))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void updateName_duplicateName_shouldReturnConflictException() {
        when(repositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(repositoryPort.existsByName("Duplicate Name")).thenReturn(Mono.just(true));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, "Duplicate Name")))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void updateName_sameName_shouldReturnUpdatedFranchise() {
        when(repositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(repositoryPort.existsByName("Original Name")).thenReturn(Mono.just(true));
        when(repositoryPort.save(any(Franchise.class))).thenReturn(Mono.just(existingFranchise));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, "Original Name")))
                .expectNext(existingFranchise)
                .verifyComplete();
    }
}