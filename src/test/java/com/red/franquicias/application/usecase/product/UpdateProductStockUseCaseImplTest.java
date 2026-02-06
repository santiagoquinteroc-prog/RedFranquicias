package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.domain.model.Product;
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
class UpdateProductStockUseCaseImplTest {
    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private UpdateProductStockUseCaseImpl useCase;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
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

        StepVerifier.create(useCase.updateStock(1L, 1L, 1L, 25))
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

        StepVerifier.create(useCase.updateStock(1L, 1L, 1L, 0))
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    void updateStock_negativeStock_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateStock(1L, 1L, 1L, -1))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("greater than or equal to 0"))
                .verify();
    }

    @Test
    void updateStock_nullStock_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateStock(1L, 1L, 1L, null))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateStock_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateStock(1L, 1L, 999L, 25))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateStock_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateStock(1L, 1L, 1L, 25))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateStock_productNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(999L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateStock(999L, 1L, 1L, 25))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateStock_productNotBelongsToBranch_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateStock(1L, 1L, 1L, 25))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }
}

