package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
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
class UpdateProductNameUseCaseImplTest {
    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private UpdateProductNameUseCaseImpl useCase;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product existingProduct;

    @BeforeEach
    void setUp() {
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

        StepVerifier.create(useCase.updateName(1L, 1L, 1L, "New Name"))
                .expectNext(updatedProduct)
                .verifyComplete();
    }

    @Test
    void updateName_emptyName_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateName(1L, 1L, 1L, ""))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateName_nullName_shouldReturnValidationException() {
        StepVerifier.create(useCase.updateName(1L, 1L, 1L, null))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void updateName_nameTooLong_shouldReturnValidationException() {
        String longName = "a".repeat(61);
        StepVerifier.create(useCase.updateName(1L, 1L, 1L, longName))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("exceed"))
                .verify();
    }

    @Test
    void updateName_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(1L, 1L, 999L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(1L, 1L, 1L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_productNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(999L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(999L, 1L, 1L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_productNotBelongsToBranch_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.updateName(1L, 1L, 1L, "New Name"))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void updateName_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.existsByNameAndBranchId("Duplicate Name", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.updateName(1L, 1L, 1L, "Duplicate Name"))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException
                        && throwable.getMessage().contains("already exists"))
                .verify();
    }

    @Test
    void updateName_sameName_shouldReturnUpdatedProduct() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.findByIdAndBranchId(1L, 1L)).thenReturn(Mono.just(existingProduct));
        when(productRepositoryPort.existsByNameAndBranchId("Original Name", 1L)).thenReturn(Mono.just(true));
        when(productRepositoryPort.save(any(Product.class))).thenReturn(Mono.just(existingProduct));

        StepVerifier.create(useCase.updateName(1L, 1L, 1L, "Original Name"))
                .expectNext(existingProduct)
                .verifyComplete();
    }
}

