package com.red.franquicias.infrastructure.config;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TestDatabaseCleaner {
    private final DatabaseClient databaseClient;

    public TestDatabaseCleaner(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Void> cleanAll() {
        return databaseClient.sql("DELETE FROM products")
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("DELETE FROM branches")
                        .fetch()
                        .rowsUpdated())
                .then(databaseClient.sql("DELETE FROM franchises")
                        .fetch()
                        .rowsUpdated())
                .then();
    }

    public Mono<Void> cleanProducts() {
        return databaseClient.sql("DELETE FROM products")
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<Void> cleanBranches() {
        return databaseClient.sql("DELETE FROM branches")
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<Void> cleanFranchises() {
        return databaseClient.sql("DELETE FROM franchises")
                .fetch()
                .rowsUpdated()
                .then();
    }
}

