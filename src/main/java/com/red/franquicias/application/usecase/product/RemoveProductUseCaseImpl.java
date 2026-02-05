package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RemoveProductUseCaseImpl implements RemoveProductUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;
    private final FranchiseRepositoryPort franchiseRepositoryPort;

    public RemoveProductUseCaseImpl(ProductRepositoryPort productRepositoryPort, BranchRepositoryPort branchRepositoryPort, FranchiseRepositoryPort franchiseRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.branchRepositoryPort = branchRepositoryPort;
        this.franchiseRepositoryPort = franchiseRepositoryPort;
    }

    @Override
    public Mono<Void> remove(Long productId, Long branchId, Long franchiseId) {
        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise -> branchRepositoryPort.findByIdAndFranchiseId(branchId, franchiseId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Branch not found or does not belong to franchise")))
                        .flatMap(branch -> productRepositoryPort.findByIdAndBranchId(productId, branchId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
                                .flatMap(product -> productRepositoryPort.deleteById(productId))));
    }
}

