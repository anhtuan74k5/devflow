package com.example.devflow;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Default application context test.
 * <p>
 * Disabled by default because it requires a running PostgreSQL instance.
 * Unit tests in service/impl/ use Mockito and do not need a database.
 */
@SpringBootTest
@Disabled("Requires running PostgreSQL database")
class DevflowApplicationTests {

	@Test
	void contextLoads() {
	}
}
