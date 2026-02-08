package com.red.franquicias.application.usecase.product;

import com.red.franquicias.application.port.out.ProductRepositoryPort;
import com.red.franquicias.domain.enums.TechnicalMessage;
import com.red.franquicias.domain.exception.BusinessException;
import com.red.franquicias.domain.exception.TechnicalException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GetTopProductsByFranchiseUseCaseImpl
        implements GetTopProductsByFranchiseUseCase {

    private final ProductRepositoryPort productRepositoryPort;

    public GetTopProductsByFranchiseUseCaseImpl(
            ProductRepositoryPort productRepositoryPort
    ) {
        this.productRepositoryPort = productRepositoryPort;
    }

    @Override
    public Mono<TopProductsResult> getTopProducts(Long franchiseId) {

        return productRepositoryPort.findTopProductsByFranchiseId(franchiseId)
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) {
                        return Mono.error(
                                new BusinessException(
                                        TechnicalMessage.FRANCHISE_NOT_FOUND
                                )
                        );
                    }

                    BranchTopProductRow first = rows.get(0);

                    List<BranchTopProduct> results = rows.stream()
                            .filter(r -> r.product_id() != null)
                            .map(r -> new BranchTopProduct(
                                    r.branch_id(),
                                    r.branch_name(),
                                    new ProductInfo(
                                            r.product_id(),
                                            r.product_name(),
                                            r.stock()
                                    )
                            ))
                            .toList();

                    return Mono.just(new TopProductsResult(
                            first.franchise_id(),
                            first.franchise_name(),
                            results
                    ));
                })
                .onErrorMap(
                        ex -> !(ex instanceof BusinessException),
                        ex -> new TechnicalException(
                                ex,
                                TechnicalMessage.TOP_PRODUCTS_QUERY_ERROR
                        )
                );
    }
}
