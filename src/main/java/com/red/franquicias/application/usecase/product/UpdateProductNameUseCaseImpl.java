package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

@Service
@Validated
public class UpdateProductNameUseCaseImpl implements UpdateProductNameUseCase {

    private final ProductRepositoryPort productRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public UpdateProductNameUseCaseImpl(
            ProductRepositoryPort productRepositoryPort,
            BranchRepositoryPort branchRepositoryPort,
            FranchiseRepositoryPort franchiseRepositoryPort
    ) {
        this.productRepositoryPort = productRepositoryPort;
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Product> updateName(
            Long productId,
            Long branchId,
            Long franchiseId,
            String name
    ) {

        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise ->
                        branchRepositoryPort.findByIdAndFranchiseId(branchId, franchiseId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Branch not found or does not belong to franchise")))
                                .flatMap(branch ->
                                        productRepositoryPort.findByIdAndBranchId(productId, branchId)
                                                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                                                .flatMap(existing ->
                                                        productRepositoryPort.existsByNameAndBranchId(name, branchId)
                                                                .flatMap(exists -> {
                                                                    if (exists && !existing.getName().equals(name)) {
                                                                        return Mono.error(new ConflictException("Product name already exists in this branch"));
                                                                    }
                                                                    existing.setName(name);
                                                                    return productRepositoryPort.save(existing);
                                                                })
                                                )
                                )
                );
    }
}
