package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.domain.validator.ProductValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateProductUseCaseImpl implements CreateProductUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public CreateProductUseCaseImpl(ProductRepositoryPort productRepositoryPort, BranchRepositoryPort branchRepositoryPort, FranchiseRepositoryPort franchiseRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Product> create(Product product, Long franchiseId) {
        try {
            ProductValidator.validateName(product.getName());
            ProductValidator.validateStock(product.getStock());
        } catch (IllegalArgumentException e) {
            return Mono.error(new ValidationException(e.getMessage()));
        }

        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise -> branchRepositoryPort.findByIdAndFranchiseId(product.getBranchId(), franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Branch not found or does not belong to franchise")))
                        .flatMap(branch -> productRepositoryPort.existsByNameAndBranchId(product.getName(), product.getBranchId())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new ConflictException("Product name already exists in this branch"));
                                    }
                                    return productRepositoryPort.save(product);
                                })));
    }
}

