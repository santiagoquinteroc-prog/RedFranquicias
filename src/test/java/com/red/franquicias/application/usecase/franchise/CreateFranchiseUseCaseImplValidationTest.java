package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.model.Franchise;
import jakarta.validation.ConstraintViolationException;
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
@ContextConfiguration(classes = CreateFranchiseUseCaseImplValidationTest.Config.class)
class CreateFranchiseUseCaseImplValidationTest {

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
            MethodValidationPostProcessor p = new MethodValidationPostProcessor();
            p.setValidator(validator);
            return p;
        }

        @Bean
        CreateFranchiseUseCase createFranchiseUseCase(FranchiseRepositoryPort repositoryPort) {
            return new CreateFranchiseUseCaseImpl(repositoryPort);
        }
    }

    @Autowired
    private CreateFranchiseUseCase useCase;

    @Autowired
    private FranchiseRepositoryPort repositoryPort;

    @Test
    void create_emptyName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.create(new Franchise(null, ""))))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.create(new Franchise(null, null))))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nameTooLong_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.create(new Franchise(null, "a".repeat(61)))))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_duplicateName_shouldReturnConflictException() {
        when(repositoryPort.existsByName("Test Franchise")).thenReturn(Mono.just(true));

        StepVerifier.create(Mono.defer(() -> useCase.create(new Franchise(null, "Test Franchise"))))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS
                )
                .verify();
    }

    @Test
    void create_valid_shouldSave() {
        Franchise saved = new Franchise(1L, "Test Franchise");
        when(repositoryPort.existsByName("Test Franchise")).thenReturn(Mono.just(false));
        when(repositoryPort.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(Mono.defer(() -> useCase.create(new Franchise(null, "Test Franchise"))))
                .expectNext(saved)
                .verifyComplete();
    }
}
