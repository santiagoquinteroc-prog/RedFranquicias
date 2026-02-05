package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Product;
import com.red.franquicias.domain.validator.ProductValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateProductStockUseCaseImpl implements UpdateProductStockUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public UpdateProductStockUseCaseImpl(ProductRepositoryPort productRepositoryPort, BranchRepositoryPort branchRepositoryPort, FranchiseRepositoryPort franchiseRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Product> updateStock(Long productId, Long branchId, Long franchiseId, Integer stock) {
        try {
            ProductValidator.validateStock(stock);
        } catch (IllegalArgumentException e) {
            return Mono.error(new ValidationException(e.getMessage()));
        }

        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise -> branchRepositoryPort.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Branch not found or does not belong to franchise")))
                        .flatMap(branch -> productRepositoryPort.findByIdAndBranchId(productId, branchId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                                .flatMap(existing -> {
                                    existing.setStock(stock);
                                    return productRepositoryPort.save(existing);
                                })));
    }
}

