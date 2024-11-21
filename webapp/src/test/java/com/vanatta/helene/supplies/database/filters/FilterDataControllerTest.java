package com.vanatta.helene.supplies.database.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FilterDataControllerTest {

  private static final Jdbi jdbiTest = TestConfiguration.jdbiTest;
  private final FilterDataController filterDataController = new FilterDataController(jdbiTest);

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void counties() {
    var response = filterDataController.getFilterData();

    // spot check we return a few expected values
    assertThat(response.getCounties()).contains("Ashe", "Watauga");
  }

  @Test
  void items() {
    var response = filterDataController.getFilterData();

    // spot check we return a few expected values
    assertThat(response.getItems()).contains("water", "new clothes", "gloves");
  }

  @Test
  void sites() {
    var response = filterDataController.getFilterData();

    // site3 is not active
    assertThat(response.getSites()).doesNotContain("site3");
    // all active sites should be returned
    assertThat(response.getSites()).hasSize(4);
  }
}
