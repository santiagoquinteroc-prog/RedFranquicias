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
class ProductRouterIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;
    
    private WebTestClient webTestClient;
    
    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    void createProduct_shouldReturn201() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Laptop\",\"stock\":10}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.branchId").isEqualTo(branchId)
                .jsonPath("$.name").isEqualTo("Laptop")
                .jsonPath("$.stock").isEqualTo(10);
    }

    @Test
    void createProduct_emptyName_shouldReturn400() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\",\"stock\":10}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createProduct_nameTooLong_shouldReturn400() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        String longName = "a".repeat(61);
        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"" + longName + "\",\"stock\":10}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createProduct_negativeStock_shouldReturn400() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Product\",\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createProduct_franchiseNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.post()
                .uri("/franchises/99999/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Product\",\"stock\":10}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void createProduct_branchNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/99999/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Product\",\"stock\":10}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void createProduct_branchNotBelongsToFranchise_shouldReturn404() {
        Long franchiseId1 = createFranchise();
        Long franchiseId2 = createFranchise();
        Long branchId = createBranch(franchiseId1);

        webTestClient.post()
                .uri("/franchises/" + franchiseId2 + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Product\",\"stock\":10}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void createProduct_duplicateName_shouldReturn409() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Duplicate Product\",\"stock\":10}")
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Duplicate Product\",\"stock\":5}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict");
    }

    @Test
    void updateProductName_shouldReturn200() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId);

        webTestClient.put()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Product Name\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(productId)
                .jsonPath("$.branchId").isEqualTo(branchId)
                .jsonPath("$.name").isEqualTo("New Product Name")
                .jsonPath("$.stock").exists();
    }

    @Test
    void updateProductName_notFound_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.put()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"New Name\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void updateProductName_duplicateName_shouldReturn409() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId1 = createProduct(franchiseId, branchId, "First Product", 10);
        Long productId2 = createProduct(franchiseId, branchId, "Second Product", 5);

        webTestClient.put()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/" + productId2)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"First Product\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.error").isEqualTo("Conflict");
    }

    @Test
    void updateProductStock_shouldReturn200() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId, "Test Product", 10);

        webTestClient.patch()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":25}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(productId)
                .jsonPath("$.branchId").isEqualTo(branchId)
                .jsonPath("$.name").isEqualTo("Test Product")
                .jsonPath("$.stock").isEqualTo(25);
    }

    @Test
    void updateProductStock_negativeStock_shouldReturn400() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId);

        webTestClient.patch()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void updateProductStock_franchiseNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId);

        webTestClient.patch()
                .uri("/franchises/99999/branches/" + branchId + "/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":25}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void updateProductStock_branchNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();

        webTestClient.patch()
                .uri("/franchises/" + franchiseId + "/branches/99999/products/99999/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":25}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void updateProductStock_productNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.patch()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/99999/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":25}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void updateProductStock_branchNotBelongsToFranchise_shouldReturn404() {
        Long franchiseId1 = createFranchise();
        Long franchiseId2 = createFranchise();
        Long branchId = createBranch(franchiseId1);
        Long productId = createProduct(franchiseId1, branchId);

        webTestClient.patch()
                .uri("/franchises/" + franchiseId2 + "/branches/" + branchId + "/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":25}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void updateProductStock_productNotBelongsToBranch_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId1 = createBranch(franchiseId);
        Long branchId2 = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId1);

        webTestClient.patch()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId2 + "/products/" + productId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":25}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void removeProduct_shouldReturn204() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId);

        webTestClient.delete()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/" + productId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void removeProduct_franchiseNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId);

        webTestClient.delete()
                .uri("/franchises/99999/branches/" + branchId + "/products/" + productId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void removeProduct_branchNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();

        webTestClient.delete()
                .uri("/franchises/" + franchiseId + "/branches/99999/products/99999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void removeProduct_productNotFound_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId = createBranch(franchiseId);

        webTestClient.delete()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products/99999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void removeProduct_branchNotBelongsToFranchise_shouldReturn404() {
        Long franchiseId1 = createFranchise();
        Long franchiseId2 = createFranchise();
        Long branchId = createBranch(franchiseId1);
        Long productId = createProduct(franchiseId1, branchId);

        webTestClient.delete()
                .uri("/franchises/" + franchiseId2 + "/branches/" + branchId + "/products/" + productId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    @Test
    void removeProduct_productNotBelongsToBranch_shouldReturn404() {
        Long franchiseId = createFranchise();
        Long branchId1 = createBranch(franchiseId);
        Long branchId2 = createBranch(franchiseId);
        Long productId = createProduct(franchiseId, branchId1);

        webTestClient.delete()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId2 + "/products/" + productId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found");
    }

    private Long createFranchise() {
        var response = webTestClient.post()
                .uri("/franchises")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Test Franchise " + System.currentTimeMillis() + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();
        return extractId(response);
    }

    private Long createBranch(Long franchiseId) {
        var response = webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"Test Branch " + System.currentTimeMillis() + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();
        return extractId(response);
    }

    private Long createProduct(Long franchiseId, Long branchId) {
        return createProduct(franchiseId, branchId, "Test Product", 10);
    }

    private Long createProduct(Long franchiseId, Long branchId, String name, Integer stock) {
        var response = webTestClient.post()
                .uri("/franchises/" + franchiseId + "/branches/" + branchId + "/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"" + name + "\",\"stock\":" + stock + "}")
                .exchange()
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();
        return extractId(response);
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

