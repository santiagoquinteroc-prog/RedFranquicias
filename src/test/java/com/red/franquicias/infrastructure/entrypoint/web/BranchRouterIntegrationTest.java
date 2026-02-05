package com.red.franquicias.infrastructure.entrypoint.web;

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
@Import(TestR2dbcConfig.class)
class BranchRouterIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;
    
    private WebTestClient webTestClient;
    
    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    void createBranch_shouldReturn201() {
        var franchiseResponse = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Test Franchise\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long franchiseId = extractId(franchiseResponse);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Branch A\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.franchiseId").isEqualTo(franchiseId)
                .jsonPath("$.name").isEqualTo("Branch A");
    }

    @Test
    void createBranch_duplicateName_shouldReturn409() {
        var franchiseResponse = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franchise For Duplicate\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long franchiseId = extractId(franchiseResponse);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Duplicate Branch\"}")
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Duplicate Branch\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict");
    }

    @Test
    void createBranch_emptyName_shouldReturn400() {
        var franchiseResponse = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franchise For Empty\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long franchiseId = extractId(franchiseResponse);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createBranch_nameTooLong_shouldReturn400() {
        var franchiseResponse = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Franchise For Long\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        Long franchiseId = extractId(franchiseResponse);

        String longName = "a".repeat(61);
        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"" + longName + "\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createBranch_franchiseNotFound_shouldReturn404() {
        webTestClient.post()
                .uri("/franchises/99999/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Branch Name\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
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

