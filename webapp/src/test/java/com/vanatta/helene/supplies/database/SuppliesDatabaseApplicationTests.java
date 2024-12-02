package com.vanatta.helene.supplies.database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"webhook.auth.secret = secret"})
class SuppliesDatabaseApplicationTests {

  @Test
  void contextLoads() {}
}
