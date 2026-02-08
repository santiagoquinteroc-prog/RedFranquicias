package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.BranchRepositoryPort;
import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
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
    public Mono<Product> updateName(Long productId, Long branchId, Long franchiseId, String name) {

        return franchiseRepositoryPort.findById(franchiseId)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)))
                .flatMap(franchise ->
                        branchRepositoryPort.findByIdAndFranchiseId(branchId, franchiseId)
                                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.BRANCH_NOT_FOUND)))
                                .flatMap(branch ->
                                        productRepositoryPort.findByIdAndBranchId(productId, branchId)
                                                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND)))
                                                .flatMap(existing ->
                                                        productRepositoryPort.existsByNameAndBranchId(name, branchId)
                                                                .flatMap(exists -> {
                                                                    if (exists && !existing.getName().equals(name)) {
                                                                        return Mono.error(new BusinessException(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS));
                                                                    }
                                                                    existing.setName(name);
                                                                    return productRepositoryPort.save(existing);
                                                                })
                                                )
                                )
                );
    }
}
