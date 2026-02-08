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
@ContextConfiguration(classes = UpdateProductStockUseCaseImplValidationTest.Config.class)
class UpdateProductStockUseCaseImplValidationTest {

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
        UpdateProductStockUseCase updateProductStockUseCase(
                ProductRepositoryPort productRepositoryPort,
                BranchRepositoryPort branchRepositoryPort,
                FranchiseRepositoryPort franchiseRepositoryPort
        ) {
            return new UpdateProductStockUseCaseImpl(productRepositoryPort, branchRepositoryPort, franchiseRepositoryPort);
        }
    }

    @Autowired
    private UpdateProductStockUseCase useCase;

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
        existingProduct = new Product(1L, 1L, "Test Product", 10);
    }

    @Test
    void updateStock_validStock_shouldReturnUpdatedProduct() {
        Product updatedProduct = new Product(1L, 1L, "Test Product", 25);
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));

        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 1L, 25)))
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    void updateStock_zeroStock_shouldReturnUpdatedProduct() {
        Product updatedProduct = new Product(1L, 1L, "Test Product", 0);
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(updatedProduct));

        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 1L, 0)))
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    void updateStock_negativeStock_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 1L, -1)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateStock_nullStock_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 1L, null)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateStock_nullProductId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateStock(null, 1L, 1L, 25)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateStock_nullBranchId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, null, 1L, 25)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateStock_nullFranchiseId_shouldThrowConstraintViolationException() {
        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, null, 25)))
                .expectError(ConstraintViolationException.class)
                .verify();
    }

    @Test
    void updateStock_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 999L, 25)))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.FRANCHISE_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateStock_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 1L, 25)))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.BRANCH_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateStock_productNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(999L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateStock(999L, 1L, 1L, 25)))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.PRODUCT_NOT_FOUND
                )
                .verify();
    }

    @Test
    void updateStock_productNotBelongsToBranch_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(Mono.defer(() -> useCase.updateStock(1L, 1L, 1L, 25)))
                .expectErrorMatches(ex ->
                        ex instanceof BusinessException be
                        && be.getTechnicalMessage() == TechnicalMessage.PRODUCT_NOT_FOUND
                )
                .verify();
    }
}