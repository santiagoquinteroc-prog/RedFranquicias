package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
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
@ContextConfiguration(classes = UpdateProductNameUseCaseImplValidationTest.Config.class)
class UpdateProductNameUseCaseImplValidationTest {

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
        UpdateProductNameUseCase updateProductNameUseCase(
                ProductRepositoryPort productRepositoryPort,
                BranchRepositoryPort branchRepositoryPort,
                FranchiseRepositoryPort franchiseRepositoryPort
        ) {
            return new UpdateProductNameUseCaseImpl(productRepositoryPort, branchRepositoryPort, franchiseRepositoryPort);
        }
    }

    @Autowired
    private UpdateProductNameUseCase useCase;

    @Autowired
    private ProductRepositoryPort productRepositoryPort;

    @Autowired
    private BranchRepositoryPort branchRepositoryPort;

    @Autowired
    private FranchiseRepositoryPort franchiseRepositoryPort;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
        Mockito.reset(productRepositoryPort, branchRepositoryPort, franchiseRepositoryPort);
        existingFranchise = new Franchise(1L, "Test Franchise");
        existingBranch = new Branch(1L, 1L, "Test Branch");
        existingProduct = new Product(1L, 1L, "Original Name", 10);
    }

    @Test
    void updateName_validName_shouldReturnUpdatedProduct() {
        Product updatedProduct = new Product(1L, 1L, "New Name", 10);
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.existsByNameAndBranchId("New Name", 1L)).thenReturn(Mono.just(false));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, "New Name")))
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    void updateName_emptyName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, "")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullName_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, null)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nameTooLong_shouldThrowConstraintViolationException() {
        String longName = "a".repeat(61);

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, longName)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullProductId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(null, 1L, 1L, "New Name")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullBranchId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, null, 1L, "New Name")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_nullFranchiseId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, null, "New Name")))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateName_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 999L, "New Name")))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.FRANCHISE_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateName_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, "New Name")))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.BRANCH_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateName_productNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(999L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(999L, 1L, 1L, "New Name")))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.PRODUCT_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateName_productNotBelongsToBranch_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, "New Name")))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.PRODUCT_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateName_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.existsByNameAndBranchId("Duplicate Name", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, "Duplicate Name")))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS
                )
                .verify();
    }

    @Test
    void updateName_sameName_shouldReturnUpdatedProduct() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.existsByNameAndBranchId("Original Name", 1L)).thenReturn(Mono.just(true));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(existingProduct));

        StepVerifier.create(Mono.defer(() -> useCase.updateName(1L, 1L, 1L, "Original Name")))
                .expectNext(existingProduct)
                .verifyComplete();
    }
}