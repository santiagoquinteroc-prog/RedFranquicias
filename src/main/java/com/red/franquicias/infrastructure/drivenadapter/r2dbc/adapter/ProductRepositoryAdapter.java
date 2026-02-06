package com.red.franquicias.infrastructure.drivenadapter.r2dbc.adapter;

import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity.ProductEntity;
import com.red.franquicias.infrastructure.drivenadapter.r2dbc.repository.ProductRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    private final ProductRepository repository;

    public ProductRepositoryAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Product> save(Product product) {
        ProductEntity entity = toEntity(product);
        return repository.save(entity)
                .map(this::toDomain);
    }

    @Override
    public Mono<Product> findById(Long id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Mono<Product> findByIdAndBranchId(Long id, Long branchId) {
        return repository.findByIdAndBranchId(id, branchId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNameAndBranchId(String name, Long branchId) {
        return repository.findByNameAndBranchId(name, branchId)
                .hasElement();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Product> findTopByBranchIdOrderByStockDesc(Long branchId) {
        return repository.findTopByBranchIdOrderByStockDesc(branchId)
                .map(this::toDomain);
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(product.getId(), product.getBranchId(), product.getName(), product.getStock());
    }

    private Product toDomain(ProductEntity entity) {
        return new Product(entity.getId(), entity.getBranchId(), entity.getName(), entity.getStock());
    }
}

