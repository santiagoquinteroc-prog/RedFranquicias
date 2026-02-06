package com.red.franquicias.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestcontainersConfig {
    private static final MySQLContainer<?> mysqlContainer;

    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);
        mysqlContainer.start();
    }

    public static ConnectionFactory getConnectionFactory() {
        String r2dbcUrl = String.format("r2dbc:mysql://%s:%d/%s",
                mysqlContainer.getHost(),
                mysqlContainer.getFirstMappedPort(),
                mysqlContainer.getDatabaseName());
        return ConnectionFactoryBuilder.withUrl(r2dbcUrl)
                .username(mysqlContainer.getUsername())
                .password(mysqlContainer.getPassword())
                .build();
    }

    public static MySQLContainer<?> getContainer() {
        return mysqlContainer;
    }
}

