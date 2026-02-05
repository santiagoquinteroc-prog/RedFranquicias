package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.exception.NotFoundException;
import com.red.franquicias.domain.model.Branch;
import com.red.franquicias.domain.model.Franchise;
import com.red.franquicias.domain.model.Product;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GetTopProductsByFranchiseUseCaseImpl implements GetTopProductsByFranchiseUseCase {
    private final FranchiseRepositoryPort franchiseRepositoryPort;
    private final BranchRepositoryPort branchRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;

    public GetTopProductsByFranchiseUseCaseImpl(FranchiseRepositoryPort franchiseRepositoryPort, BranchRepositoryPort branchRepositoryPort, ProductRepositoryPort productRepositoryPort) {
        this.franchiseRepositoryPort = franchiseRepositoryPort;
        this.branchRepositoryPort = branchRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<TopProductsResult> getTopProducts(Long franchiseId) {
        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franchise not found")))
                .flatMap(franchise -> branchRepositoryPort.findByFranchiseId(franchiseId)
                        .flatMap(branch -> productRepositoryPort.findTopByBranchIdOrderByStockDesc(branch.getId())
                                .map(product -> new BranchTopProduct(
                                        branch.getId(),
                                        branch.getName(),
                                        new ProductInfo(product.getId(), product.getName(), product.getStock())
                                ))
                                .switchIfEmpty(Mono.empty()))
                        .collectList()
                        .map(results -> new TopProductsResult(franchise.getId(), franchise.getName(), results)));
    }
}

