package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.domain.model.Product;
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
@ContextConfiguration(classes = CreateProductUseCaseImplValidationTest.Config.class)
class CreateProductUseCaseImplValidationTest {

    @Configuration
    static class Config {

        @Bean
        ProductRepositoryPort productRepositoryPort() {
            return Mockito.mock(ProductRepositoryPort.class);
        }

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
        CreateProductUseCase createProductUseCase(
                ProductRepositoryPort productRepositoryPort,
                BranchRepositoryPort branchRepositoryPort,
                FranchiseRepositoryPort franchiseRepositoryPort
        ) {
            return new CreateProductUseCaseImpl(productRepositoryPort, branchRepositoryPort, franchiseRepositoryPort);
        }
    }

    @Autowired
    private CreateProductUseCase useCase;

    @Autowired
    private ProductRepositoryPort productRepositoryPort;

    @Autowired
    private BranchRepositoryPort branchRepositoryPort;

    @Autowired
    private FranchiseRepositoryPort franchiseRepositoryPort;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product validProduct;

    @BeforeEach
    void setUp() {
        Mockito.reset(productRepositoryPort, branchRepositoryPort, franchiseRepositoryPort);
        existingFranchise = new Franchise(1L, "Test Franchise");
        existingBranch = new Branch(1L, 1L, "Test Branch");
        validProduct = new Product(null, 1L, "Test Product", 10);
    }

    @Test
    void create_validProduct_shouldReturnCreatedProduct() {
        Product savedProduct = new Product(1L, 1L, "Test Product", 10);
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.existsByNameAndBranchId("Test Product", 1L)).thenReturn(Mono.just(false));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(savedProduct));

        StepVerifier.create(Mono.defer(() -> useCase.create(validProduct, 1L)))
                .expectNext(savedProduct)
                .verifyComplete();
    }

    @Test
    void create_emptyName_shouldThrowConstraintViolationException() {
        Product product = new Product(null, 1L, "", 10);

        StepVerifier.create(Mono.defer(() -> useCase.create(product, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullName_shouldThrowConstraintViolationException() {
        Product product = new Product(null, 1L, null, 10);

        StepVerifier.create(Mono.defer(() -> useCase.create(product, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nameTooLong_shouldThrowConstraintViolationException() {
        String longName = "a".repeat(61);
        Product product = new Product(null, 1L, longName, 10);

        StepVerifier.create(Mono.defer(() -> useCase.create(product, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_negativeStock_shouldThrowConstraintViolationException() {
        Product product = new Product(null, 1L, "Test Product", -1);

        StepVerifier.create(Mono.defer(() -> useCase.create(product, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullStock_shouldThrowConstraintViolationException() {
        Product product = new Product(null, 1L, "Test Product", null);

        StepVerifier.create(Mono.defer(() -> useCase.create(product, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullProduct_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.create(null, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullFranchiseId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.create(validProduct, null)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_nullBranchId_shouldThrowConstraintViolationException() {
        Product product = new Product(null, null, "Test Product", 10);

        StepVerifier.create(Mono.defer(() -> useCase.create(product, 1L)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void create_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.create(validProduct, 999L)))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void create_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.create(validProduct, 1L)))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void create_branchNotBelongsToFranchise_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.create(validProduct, 1L)))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void create_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.existsByNameAndBranchId("Test Product", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(Mono.defer(() -> useCase.create(validProduct, 1L)))
                .expectError(ConflictException.class)
                .verify();
    }
}