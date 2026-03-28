package com.capg.documentservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires MySQL and RabbitMQ — run with Docker or integration profile")
class DocumentServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
