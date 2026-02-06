package com.red.franquicias.infrastructure.drivenadapter.r2dbc.adapter;

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
class FranchiseRepositoryAdapterTest {
    @Autowired
    private FranchiseRepositoryAdapter adapter;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        databaseClient.sql("CREATE TABLE IF NOT EXISTS franchises (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(60) UNIQUE NOT NULL)")
                .fetch()
                .rowsUpdated()
                .then(databaseCleaner.cleanFranchises())
                .block();
    }

    @Test
    void save_newFranchise_shouldAssignId() {
        Franchise franchise = new Franchise(null, "Test Franchise");

        StepVerifier.create(adapter.save(franchise))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals("Test Franchise", saved.getName());
                })
                .verifyComplete();
    }

    @Test
    void save_updateExistingFranchise_shouldUpdateName() {
        Franchise created = adapter.save(new Franchise(null, "Original Name")).block();
        Franchise updated = new Franchise(created.getId(), "Updated Name");

        StepVerifier.create(adapter.save(updated))
                .assertNext(saved -> {
                    assertEquals(created.getId(), saved.getId());
                    assertEquals("Updated Name", saved.getName());
                })
                .verifyComplete();
    }

    @Test
    void findById_existingFranchise_shouldReturnFranchise() {
        Franchise created = adapter.save(new Franchise(null, "Test Franchise")).block();

        StepVerifier.create(adapter.findById(created.getId()))
                .assertNext(found -> {
                    assertEquals(created.getId(), found.getId());
                    assertEquals("Test Franchise", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void findById_nonExistentFranchise_shouldReturnEmpty() {
        StepVerifier.create(adapter.findById(999L))
                .verifyComplete();
    }

    @Test
    void existsByName_existingName_shouldReturnTrue() {
        adapter.save(new Franchise(null, "Existing Franchise")).block();

        StepVerifier.create(adapter.existsByName("Existing Franchise"))
                .assertNext(exists -> assertTrue(exists))
                .verifyComplete();
    }

    @Test
    void existsByName_nonExistentName_shouldReturnFalse() {
        StepVerifier.create(adapter.existsByName("Non Existent"))
                .assertNext(exists -> assertFalse(exists))
                .verifyComplete();
    }
}

