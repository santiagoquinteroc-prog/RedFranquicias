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
class FranchiseRouterIntegrationTest {
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
    void createFranchise_shouldReturn201() {
        webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franchise A\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("Franchise A");
    }

    @Test
    void createFranchise_duplicateName_shouldReturn409() {
        webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Duplicate Franchise\"}")
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Duplicate Franchise\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict");
    }

    @Test
    void createFranchise_emptyName_shouldReturn400() {
        webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createFranchise_nameTooLong_shouldReturn400() {
        String longName = "a".repeat(61);
        webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"" + longName + "\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void updateFranchiseName_shouldReturn200() {
        var createResponse = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Original Name\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long id = extractId(createResponse);

        webTestClient.put()
                .uri("/franchises/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo("New Name");
    }

    @Test
    void updateFranchiseName_notFound_shouldReturn404() {
        webTestClient.put()
                .uri("/franchises/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void updateFranchiseName_duplicateName_shouldReturn409() {
        webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"First Franchise\"}")
                .exchange()
                .expectStatus().isCreated();

        var createResponse = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Second Franchise\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long id = extractId(createResponse);

        webTestClient.put()
                .uri("/franchises/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"First Franchise\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict");
    }

    private Long extractId(String jsonResponse) {
        int idIndex = jsonResponse.indexOf("\"id\":");
        if (idIndex == -1) {
            return null;
        }
        String idStr = jsonResponse.substring(idIndex + 5);
        int commaIndex = idStr.indexOf(",");
        int braceIndex = idStr.indexOf("}");
        int endIndex = commaIndex != -1 && commaIndex < braceIndex ? commaIndex : braceIndex;
        idStr = idStr.substring(0, endIndex);
        return Long.parseLong(idStr.trim());
    }
}

