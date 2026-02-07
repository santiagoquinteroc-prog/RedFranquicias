package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopProductsByFranchiseUseCaseImplTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @InjectMocks
    private GetTopProductsByFranchiseUseCaseImpl useCase;

    @Test
    void getTopProducts_franchiseWithBranchesAndProducts_shouldReturnTopProducts() {
        var row1 = new BranchTopProductRow(1L, "Test Franchise", 1L, "Branch 1", 10L, "Product A", 50);
        var row2 = new BranchTopProductRow(1L, "Test Franchise", 2L, "Branch 2", 20L, "Product B", 30);
        var row3 = new BranchTopProductRow(1L, "Test Franchise", 3L, "Branch 3", null, null, null);

        when(productRepositoryPort.findTopProductsByFranchiseId(1L))
                .thenReturn(Flux.just(row1, row2, row3));

        StepVerifier.create(useCase.getTopProducts(1L))
                .assertNext(result -> {
                    assertEquals(1L, result.franchiseId());
                    assertEquals("Test Franchise", result.franchiseName());

                    assertEquals(2, result.results().size());

                    assertTrue(result.results().stream().anyMatch(r ->
                            r.branchId().equals(1L)
                            && r.product() != null
                            && r.product().stock().equals(50)
                    ));

                    assertTrue(result.results().stream().anyMatch(r ->
                            r.branchId().equals(2L)
                            && r.product() != null
                            && r.product().stock().equals(30)
                    ));

                    assertFalse(result.results().stream().anyMatch(r -> r.branchId().equals(3L)));
                })
                .verifyComplete();
    }

    @Test
    void getTopProducts_franchiseWithBranchesButNoProducts_shouldReturnEmptyList() {
        var row1 = new BranchTopProductRow(1L, "Test Franchise", 1L, "Branch 1", null, null, null);
        var row2 = new BranchTopProductRow(1L, "Test Franchise", 2L, "Branch 2", null, null, null);

        when(productRepositoryPort.findTopProductsByFranchiseId(1L))
                .thenReturn(Flux.just(row1, row2));

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
        var row1 = new BranchTopProductRow(1L, "Test Franchise", 1L, "Branch 1", 10L, "Product A", 50);
        var row2 = new BranchTopProductRow(1L, "Test Franchise", 2L, "Branch 2", null, null, null);

        when(productRepositoryPort.findTopProductsByFranchiseId(1L))
                .thenReturn(Flux.just(row1, row2));

        StepVerifier.create(useCase.getTopProducts(1L))
                .assertNext(result -> {
                    assertEquals(1L, result.franchiseId());
                    assertEquals("Test Franchise", result.franchiseName());

                    assertEquals(1, result.results().size());

                    var b1 = result.results().get(0);
                    assertEquals(1L, b1.branchId());
                    assertEquals("Branch 1", b1.branchName());
                    assertNotNull(b1.product());
                    assertEquals(50, b1.product().stock());
                })
                .verifyComplete();
    }

    @Test
    void getTopProducts_franchiseNotFound_shouldReturnNotFoundException() {
        when(productRepositoryPort.findTopProductsByFranchiseId(999L))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.getTopProducts(999L))
                .expectErrorMatches(ex -> ex instanceof NotFoundException
                                          && ex.getMessage().toLowerCase().contains("not found"))
                .verify();
    }
}
