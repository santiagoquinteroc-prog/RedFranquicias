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
class CreateProductUseCaseImplTest {
    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @InjectMocks
    private CreateProductUseCaseImpl useCase;

    private Franchise existingFranchise;
    private Branch existingBranch;
    private Product validProduct;

    @BeforeEach
    void setUp() {
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

        StepVerifier.create(useCase.create(validProduct, 1L))
                .expectNext(savedProduct)
                .verifyComplete();
    }

    @Test
    void create_emptyName_shouldReturnValidationException() {
        Product product = new Product(null, 1L, "", 10);

        StepVerifier.create(useCase.create(product, 1L))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_nullName_shouldReturnValidationException() {
        Product product = new Product(null, 1L, null, 10);

        StepVerifier.create(useCase.create(product, 1L))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_nameTooLong_shouldReturnValidationException() {
        String longName = "a".repeat(61);
        Product product = new Product(null, 1L, longName, 10);

        StepVerifier.create(useCase.create(product, 1L))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("exceed"))
                .verify();
    }

    @Test
    void create_negativeStock_shouldReturnValidationException() {
        Product product = new Product(null, 1L, "Test Product", -1);

        StepVerifier.create(useCase.create(product, 1L))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("greater than or equal to 0"))
                .verify();
    }

    @Test
    void create_nullStock_shouldReturnValidationException() {
        Product product = new Product(null, 1L, "Test Product", null);

        StepVerifier.create(useCase.create(product, 1L))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(validProduct, 999L))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void create_branchNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(validProduct, 1L))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void create_branchNotBelongsToFranchise_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(validProduct, 1L))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }

    @Test
    void create_duplicateName_shouldReturnConflictException() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByIdAndFranchiseId(1L, 1L)).thenReturn(Mono.just(existingBranch));
        when(productRepositoryPort.existsByNameAndBranchId("Test Product", 1L)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.create(validProduct, 1L))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException
                        && throwable.getMessage().contains("already exists"))
                .verify();
    }
}

