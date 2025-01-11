package com.vanatta.helene.supplies.database;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SimpleHtmlControllerTest {

  @Test
  void fetchSiteDescription() {
    var result =
        SimpleHtmlController.fetchDeploymentDescription(
            TestConfiguration.jdbiTest, "WNC-Supply-Sites.com");
    assertThat(result.getSiteDescription()).isEqualTo("Hurricane Helene Disaster Relief");
    assertThat(result.getContactUsLink()).isNotNull();

    result =
        SimpleHtmlController.fetchDeploymentDescription(
            TestConfiguration.jdbiTest, "SoCal-Supply-Sites.com");
    assertThat(result.getSiteDescription()).isEqualTo("LA Fires Disaster Relief");
    assertThat(result.getContactUsLink()).isNotNull();
  }
}
