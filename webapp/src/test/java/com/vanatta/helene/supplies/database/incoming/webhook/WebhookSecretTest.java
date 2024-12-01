package com.vanatta.helene.supplies.database.incoming.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WebhookSecretTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void badKey() {
    assertThat(new WebhookSecret(TestConfiguration.jdbiTest).isValid("bad")).isFalse();
    assertThat(new WebhookSecret(TestConfiguration.jdbiTest).isValid("")).isFalse();
    assertThat(new WebhookSecret(TestConfiguration.jdbiTest).isValid(null)).isFalse();
  }


  @Test
  void validKey() {
    assertThat(new WebhookSecret(TestConfiguration.jdbiTest).isValid("open-sesame")).isTrue();
  }
}
