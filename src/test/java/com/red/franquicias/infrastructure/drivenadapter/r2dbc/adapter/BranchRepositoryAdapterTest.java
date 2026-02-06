package com.red.franquicias.infrastructure.drivenadapter.r2dbc.adapter;

import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
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
class BranchRepositoryAdapterTest {
    @Autowired
    private BranchRepositoryAdapter adapter;

    @Autowired
    private FranchiseRepositoryAdapter franchiseAdapter;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @Autowired
    private DatabaseClient databaseClient;

    private Franchise testFranchise;
    private Franchise otherFranchise;

    @BeforeEach
    void setUp() {
        databaseClient.sql("CREATE TABLE IF NOT EXISTS franchises (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(60) UNIQUE NOT NULL)")
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("CREATE TABLE IF NOT EXISTS branches (id BIGINT AUTO_INCREMENT PRIMARY KEY, franchise_id BIGINT NOT NULL, name VARCHAR(60) NOT NULL, FOREIGN KEY (franchise_id) REFERENCES franchises(id), UNIQUE(franchise_id, name))")
                        .fetch()
                        .rowsUpdated())
                .then(databaseCleaner.cleanAll())
                .block();

        testFranchise = franchiseAdapter.save(new Franchise(null, "Test Franchise")).block();
        otherFranchise = franchiseAdapter.save(new Franchise(null, "Other Franchise")).block();
    }

    @Test
    void save_newBranch_shouldAssignId() {
        Branch branch = new Branch(null, testFranchise.getId(), "Test Branch");

        StepVerifier.create(adapter.save(branch))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals(testFranchise.getId(), saved.getFranchiseId());
                    assertEquals("Test Branch", saved.getName());
                })
                .verifyComplete();
    }

    @Test
    void save_updateExistingBranch_shouldUpdateName() {
        Branch created = adapter.save(new Branch(null, testFranchise.getId(), "Original Name")).block();
        Branch updated = new Branch(created.getId(), testFranchise.getId(), "Updated Name");

        StepVerifier.create(adapter.save(updated))
                .assertNext(saved -> {
                    assertEquals(created.getId(), saved.getId());
                    assertEquals("Updated Name", saved.getName());
                })
                .verifyComplete();
    }

    @Test
    void findById_existingBranch_shouldReturnBranch() {
        Branch created = adapter.save(new Branch(null, testFranchise.getId(), "Test Branch")).block();

        StepVerifier.create(adapter.findById(created.getId()))
                .assertNext(found -> {
                    assertEquals(created.getId(), found.getId());
                    assertEquals("Test Branch", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void findById_nonExistentBranch_shouldReturnEmpty() {
        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }

    @Test
    void findByIdAndFranchiseId_correctMatch_shouldReturnBranch() {
        Branch created = adapter.save(new Branch(null, testFranchise.getId(), "Test Branch")).block();

        StepVerifier.create(adapter.findByIdAndFranchiseId(created.getId(), testFranchise.getId()))
                .assertNext(found -> {
                    assertEquals(created.getId(), found.getId());
                    assertEquals(testFranchise.getId(), found.getFranchiseId());
                })
                .verifyComplete();
    }

    @Test
    void findByIdAndFranchiseId_wrongFranchise_shouldReturnEmpty() {
        Branch created = adapter.save(new Branch(null, testFranchise.getId(), "Test Branch")).block();

        StepVerifier.create(adapter.findByIdAndFranchiseId(created.getId(), otherFranchise.getId()))
                .verifyComplete();
    }

    @Test
    void findByIdAndFranchiseId_nonExistent_shouldReturnEmpty() {
        StepVerifier.create(adapter.findByIdAndFranchiseId(999L, testFranchise.getId()))
                .verifyComplete();
    }

    @Test
    void existsByNameAndFranchiseId_existingName_shouldReturnTrue() {
        adapter.save(new Branch(null, testFranchise.getId(), "Existing Branch")).block();

        StepVerifier.create(adapter.existsByNameAndFranchiseId("Existing Branch", testFranchise.getId()))
                .assertNext(exists -> assertTrue(exists))
                .verifyComplete();
    }

    @Test
    void existsByNameAndFranchiseId_nonExistentName_shouldReturnFalse() {
        StepVerifier.create(adapter.existsByNameAndFranchiseId("Non Existent", testFranchise.getId()))
                .assertNext(exists -> assertFalse(exists))
                .verifyComplete();
    }

    @Test
    void findByFranchiseId_multipleBranches_shouldReturnAllBranches() {
        Branch branch1 = adapter.save(new Branch(null, testFranchise.getId(), "Branch 1")).block();
        Branch branch2 = adapter.save(new Branch(null, testFranchise.getId(), "Branch 2")).block();
        adapter.save(new Branch(null, otherFranchise.getId(), "Other Branch")).block();

        StepVerifier.create(adapter.findByFranchiseId(testFranchise.getId()))
                .assertNext(branch -> assertTrue(branch.getId().equals(branch1.getId()) || branch.getId().equals(branch2.getId())))
                .assertNext(branch -> assertTrue(branch.getId().equals(branch1.getId()) || branch.getId().equals(branch2.getId())))
                .verifyComplete();
    }

    @Test
    void findByFranchiseId_noBranches_shouldReturnEmpty() {
        StepVerifier.create(adapter.findByFranchiseId(testFranchise.getId()))
                .verifyComplete();
    }
}

