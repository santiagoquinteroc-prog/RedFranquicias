package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopProductsByFranchiseUseCaseImplTest {
    @Mock
    private FranchiseRepositoryPort franchiseRepositoryPort;

    @Mock
    private BranchRepositoryPort branchRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private GetTopProductsByFranchiseUseCaseImpl useCase;

    private Franchise existingFranchise;
    private Branch branch1;
    private Branch branch2;
    private Branch branch3;

    @BeforeEach
    void setUp() {
        existingFranchise = new Franchise(1L, "Test Franchise");
        branch1 = new Branch(1L, 1L, "Branch 1");
        branch2 = new Branch(2L, 1L, "Branch 2");
        branch3 = new Branch(3L, 1L, "Branch 3");
    }

    @Test
    void getTopProducts_franchiseWithBranchesAndProducts_shouldReturnTopProducts() {
        Product topProduct1 = new Product(1L, 1L, "Product A", 50);
        Product topProduct2 = new Product(2L, 2L, "Product B", 30);

        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByFranchiseId(1L)).thenReturn(Flux.just(branch1, branch2, branch3));
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(1L)).thenReturn(Mono.just(topProduct1));
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(2L)).thenReturn(Mono.just(topProduct2));
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(3L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getTopProducts(1L))
                .assertNext(result -> {
                    assertEquals(1L, result.franchiseId());
                    assertEquals("Test Franchise", result.franchiseName());
                    assertEquals(2, result.results().size());
                    assertTrue(result.results().stream().anyMatch(r -> r.branchId().equals(1L) && r.product().stock().equals(50)));
                    assertTrue(result.results().stream().anyMatch(r -> r.branchId().equals(2L) && r.product().stock().equals(30)));
                })
                .verifyComplete();
    }

    @Test
    void getTopProducts_franchiseWithBranchesButNoProducts_shouldReturnEmptyList() {
        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByFranchiseId(1L)).thenReturn(Flux.just(branch1, branch2));
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(1L)).thenReturn(Mono.empty());
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(2L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getTopProducts(1L))
                .assertNext(result -> {
                    assertEquals(1L, result.franchiseId());
                    assertEquals("Test Franchise", result.franchiseName());
                    assertTrue(result.results().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getTopProducts_franchiseWithSomeBranchesHavingProducts_shouldReturnOnlyBranchesWithProducts() {
        Product topProduct1 = new Product(1L, 1L, "Product A", 50);

        when(franchiseRepositoryPort.findById(1L)).thenReturn(Mono.just(existingFranchise));
        when(branchRepositoryPort.findByFranchiseId(1L)).thenReturn(Flux.just(branch1, branch2));
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(1L)).thenReturn(Mono.just(topProduct1));
        when(productRepositoryPort.findTopByBranchIdOrderByStockDesc(2L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getTopProducts(1L))
                .assertNext(result -> {
                    assertEquals(1L, result.franchiseId());
                    assertEquals("Test Franchise", result.franchiseName());
                    assertEquals(1, result.results().size());
                    assertEquals(1L, result.results().get(0).branchId());
                    assertEquals("Branch 1", result.results().get(0).branchName());
                    assertEquals(50, result.results().get(0).product().stock());
                })
                .verifyComplete();
    }

    @Test
    void getTopProducts_franchiseNotFound_shouldReturnNotFoundException() {
        when(franchiseRepositoryPort.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getTopProducts(999L))
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().contains("not found"))
                .verify();
    }
}


