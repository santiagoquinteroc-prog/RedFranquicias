package com.red.franquicias.application.usecase.franchise;

import com.red.franquicias.application.port.out.FranchiseRepositoryPort;
import com.red.franquicias.domain.exception.ConflictException;
import com.red.franquicias.domain.exception.ValidationException;
import com.red.franquicias.domain.model.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateFranchiseUseCaseImplTest {
    @Mock
    private FranchiseRepositoryPort repositoryPort;

    @InjectMocks
    private CreateFranchiseUseCaseImpl useCase;

    private Franchise validFranchise;

    @BeforeEach
    void setUp() {
        validFranchise = new Franchise(null, "Test Franchise");
    }

    @Test
    void create_validFranchise_shouldReturnCreatedFranchise() {
        Franchise savedFranchise = new Franchise(1L, "Test Franchise");
        when(repositoryPort.existsByName("Test Franchise")).thenReturn(Mono.just(false));
        when(repositoryPort.save(any(Franchise.class))).thenReturn(Mono.just(savedFranchise));

        StepVerifier.create(useCase.create(validFranchise))
                .expectNext(savedFranchise)
                .verifyComplete();
    }

    @Test
    void create_emptyName_shouldReturnValidationException() {
        Franchise franchise = new Franchise(null, "");

        StepVerifier.create(useCase.create(franchise))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_nullName_shouldReturnValidationException() {
        Franchise franchise = new Franchise(null, null);

        StepVerifier.create(useCase.create(franchise))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("required"))
                .verify();
    }

    @Test
    void create_nameTooLong_shouldReturnValidationException() {
        String longName = "a".repeat(61);
        Franchise franchise = new Franchise(null, longName);

        StepVerifier.create(useCase.create(franchise))
                .expectErrorMatches(throwable -> throwable instanceof ValidationException
                        && throwable.getMessage().contains("exceed"))
                .verify();
    }

    @Test
    void create_duplicateName_shouldReturnConflictException() {
        when(repositoryPort.existsByName("Test Franchise")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.create(validFranchise))
                .expectErrorMatches(throwable -> throwable instanceof ConflictException
                        && throwable.getMessage().contains("already exists"))
                .verify();
    }
}

