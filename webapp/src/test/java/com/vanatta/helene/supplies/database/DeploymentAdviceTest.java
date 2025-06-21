package com.vanatta.helene.supplies.database;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeploymentAdviceTest {

  @ParameterizedTest
  @CsvSource({"wnc-supply-sites.com,WNC & Appalachian", "socal-supply-sites.com,SoCal"})
  void getShortNameForHost(String input, String output) {
    assertThat(DeploymentAdvice.getShortNameForHost(jdbiTest, input)).isEqualTo(output);
  }

  @Test
  void fetchStateListForHost() {
    assertThat(DeploymentAdvice.fetchStateListForHost(jdbiTest, "wnc-supply-sites.com"))
        .contains("NC", "TN");
    assertThat(DeploymentAdvice.fetchStateListForHost(jdbiTest, "socal-supply-sites.com"))
        .contains("CA");
  }
}
