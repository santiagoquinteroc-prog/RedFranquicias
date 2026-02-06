package com.red.franquicias;

import com.red.franquicias.infrastructure.config.TestR2dbcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration")
@Import(TestR2dbcConfig.class)
class FranquiciasApplicationTests {

	@Test
	void contextLoads() {
	}

}
