package com.red.franquicias.infrastructure.entrypoint.web;

import com.red.franquicias.infrastructure.config.TestDatabaseCleaner;
import com.red.franquicias.infrastructure.config.TestR2dbcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration")
@Import({TestR2dbcConfig.class, TestDatabaseCleaner.class})
class HealthRouterIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        databaseCleaner.cleanAll().block();
    }

    @Test
    void health_shouldReturn200() {
        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }
}

