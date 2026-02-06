package com.red.franquicias.infrastructure.drivenadapter.r2dbc.adapter;

import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.config.TestDatabaseCleaner;
import com.red.franquicias.infrastructure.config.TestR2dbcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({TestR2dbcConfig.class, TestDatabaseCleaner.class})
class ProductRepositoryAdapterTest {
    @Autowired
    private ProductRepositoryAdapter adapter;

    @Autowired
    private BranchRepositoryAdapter branchAdapter;

    @Autowired
    private FranchiseRepositoryAdapter franchiseAdapter;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @Autowired
    private DatabaseClient databaseClient;

    private Franchise testFranchise;
    private Branch testBranch;
    private Branch otherBranch;

    @BeforeEach
    void setUp() {
        databaseClient.sql("CREATE TABLE IF NOT EXISTS franchises (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(60) UNIQUE NOT NULL)")
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("CREATE TABLE IF NOT EXISTS branches (id BIGINT AUTO_INCREMENT PRIMARY KEY, franchise_id BIGINT NOT NULL, name VARCHAR(60) NOT NULL, FOREIGN KEY (franchise_id) REFERENCES franchises(id), UNIQUE(franchise_id, name))")
                        .fetch()
                        .rowsUpdated())
                .then(databaseClient.sql("CREATE TABLE IF NOT EXISTS products (id BIGINT AUTO_INCREMENT PRIMARY KEY, branch_id BIGINT NOT NULL, name VARCHAR(60) NOT NULL, stock INT NOT NULL, FOREIGN KEY (branch_id) REFERENCES branches(id), UNIQUE(branch_id, name))")
                        .fetch()
                        .rowsUpdated())
                .then(databaseCleaner.cleanAll())
                .block();

        testFranchise = franchiseAdapter.save(new Franchise(null, "Test Franchise")).block();
        testBranch = branchAdapter.save(new Branch(null, testFranchise.getId(), "Test Branch")).block();
        otherBranch = branchAdapter.save(new Branch(null, testFranchise.getId(), "Other Branch")).block();
    }

    @Test
    void save_newProduct_shouldAssignId() {
        Product product = new Product(null, testBranch.getId(), "Test Product", 10);

        StepVerifier.create(adapter.save(product))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals(testBranch.getId(), saved.getBranchId());
                    assertEquals("Test Product", saved.getName());
                    assertEquals(10, saved.getStock());
                })
                .verifyComplete();
    }

    @Test
    void save_updateExistingProduct_shouldUpdateFields() {
        Product created = adapter.save(new Product(null, testBranch.getId(), "Original Name", 10)).block();
        Product updated = new Product(created.getId(), testBranch.getId(), "Updated Name", 25);

        StepVerifier.create(adapter.save(updated))
                .assertNext(saved -> {
                    assertEquals(created.getId(), saved.getId());
                    assertEquals("Updated Name", saved.getName());
                    assertEquals(25, saved.getStock());
                })
                .verifyComplete();
    }

    @Test
    void findById_existingProduct_shouldReturnProduct() {
        Product created = adapter.save(new Product(null, testBranch.getId(), "Test Product", 10)).block();

        StepVerifier.create(adapter.findById(created.getId()))
                .assertNext(found -> {
                    assertEquals(created.getId(), found.getId());
                    assertEquals("Test Product", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void findById_nonExistentProduct_shouldReturnEmpty() {
        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }

    @Test
    void findByIdAndBranchId_correctMatch_shouldReturnProduct() {
        Product created = adapter.save(new Product(null, testBranch.getId(), "Test Product", 10)).block();

        StepVerifier.create(adapter.findByIdAndBranchId(created.getId(), testBranch.getId()))
                .assertNext(found -> {
                    assertEquals(created.getId(), found.getId());
                    assertEquals(testBranch.getId(), found.getBranchId());
                })
                .verifyComplete();
    }

    @Test
    void findByIdAndBranchId_wrongBranch_shouldReturnEmpty() {
        Product created = adapter.save(new Product(null, testBranch.getId(), "Test Product", 10)).block();

        StepVerifier.create(adapter.findByIdAndBranchId(created.getId(), otherBranch.getId()))
                .verifyComplete();
    }

    @Test
    void findByIdAndBranchId_nonExistent_shouldReturnEmpty() {
        StepVerifier.create(adapter.findByIdAndBranchId(999L, testBranch.getId()))
                .verifyComplete();
    }

    @Test
    void existsByNameAndBranchId_existingName_shouldReturnTrue() {
        adapter.save(new Product(null, testBranch.getId(), "Existing Product", 10)).block();

        StepVerifier.create(adapter.existsByNameAndBranchId("Existing Product", testBranch.getId()))
                .assertNext(exists -> assertTrue(exists))
                .verifyComplete();
    }

    @Test
    void existsByNameAndBranchId_nonExistentName_shouldReturnFalse() {
        StepVerifier.create(adapter.existsByNameAndBranchId("Non Existent", testBranch.getId()))
                .assertNext(exists -> assertFalse(exists))
                .verifyComplete();
    }

    @Test
    void deleteById_existingProduct_shouldDeleteProduct() {
        Product created = adapter.save(new Product(null, testBranch.getId(), "To Delete", 10)).block();

        StepVerifier.create(adapter.deleteById(created.getId()))
                .verifyComplete();

        StepVerifier.create(adapter.findById(created.getId()))
                .verifyComplete();
    }

    @Test
    void findTopByBranchIdOrderByStockDesc_multipleProducts_shouldReturnHighestStock() {
        adapter.save(new Product(null, testBranch.getId(), "Product Low", 10)).block();
        Product highStock = adapter.save(new Product(null, testBranch.getId(), "Product High", 50)).block();
        adapter.save(new Product(null, testBranch.getId(), "Product Medium", 30)).block();

        StepVerifier.create(adapter.findTopByBranchIdOrderByStockDesc(testBranch.getId()))
                .assertNext(top -> {
                    assertEquals(50, top.getStock());
                    assertEquals(highStock.getId(), top.getId());
                })
                .verifyComplete();
    }

    @Test
    void findTopByBranchIdOrderByStockDesc_noProducts_shouldReturnEmpty() {
        StepVerifier.create(adapter.findTopByBranchIdOrderByStockDesc(testBranch.getId()))
                .verifyComplete();
    }
}

