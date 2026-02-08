package com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository;

import com.red.franquicias.application.usecase.product.BranchTopProductRow;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, Long> {

    Mono<ProductEntity> findByIdAndBranchId(Long id, Long branchId);

    Mono<ProductEntity> findByNameAndBranchId(String name, Long branchId);

    @Query("SELECT * FROM products WHERE branch_id = :branchId ORDER BY stock DESC LIMIT 1")
    Mono<ProductEntity> findTopByBranchIdOrderByStockDesc(Long branchId);

    @Query("""
            SELECT
              f.id   AS franchise_id,
              f.name AS franchise_name,
              b.id   AS branch_id,
              b.name AS branch_name,
              p.id   AS product_id,
              p.name AS product_name,
              p.stock AS stock
            FROM franchises f
            JOIN branches b ON b.franchise_id = f.id
            LEFT JOIN (
              SELECT p1.*
              FROM products p1
              JOIN (
                SELECT branch_id, MAX(stock) AS max_stock
                FROM products
                GROUP BY branch_id
              ) mx ON mx.branch_id = p1.branch_id AND mx.max_stock = p1.stock
            ) p ON p.branch_id = b.id
            WHERE f.id = :franchiseId
            ORDER BY b.id
            """)
    Flux<BranchTopProductRow> findTopProductsByFranchiseId(Long franchiseId);


}
