package com.red.franquicias.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@TestConfiguration
@Import(TestcontainersConfig.class)
public class TestR2dbcConfig extends AbstractR2dbcConfiguration {
    @Override
    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return TestcontainersConfig.getConnectionFactory();
    }
}

