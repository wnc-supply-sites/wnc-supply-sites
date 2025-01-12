package com.vanatta.helene.supplies.database;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DomainNameInterceptorTest {

  @Test
  void fetchValidDomains() {
    var results = DomainNameInterceptor.fetchValidDomains(TestConfiguration.jdbiTest);

    assertThat(results).contains("localhost", "wnc-supply-sites.com", "socal-supply-sites.com");
  }
}
