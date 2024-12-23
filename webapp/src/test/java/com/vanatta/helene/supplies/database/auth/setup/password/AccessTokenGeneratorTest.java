package com.vanatta.helene.supplies.database.auth.setup.password;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccessTokenGeneratorTest {
  AccessTokenGenerator accessTokenGenerator = new AccessTokenGenerator();

  @Test
  void generate() {
    String value = accessTokenGenerator.generate();
    assertThat(value).hasSize(6);
  }

  @Test
  void generateValuesChange() {
    String value = accessTokenGenerator.generate();
    String nextValue = accessTokenGenerator.generate();
    assertThat(value).isNotEqualTo(nextValue);
  }
}
