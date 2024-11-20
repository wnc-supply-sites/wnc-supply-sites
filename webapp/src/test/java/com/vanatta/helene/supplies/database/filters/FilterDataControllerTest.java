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

    // site3 is not active, site4 has no items
    assertThat(response.getSites()).doesNotContain("site3", "site4");
    // all active sites with items should be returned, there are 2 of them in the test data
    assertThat(response.getSites()).hasSize(2);
  }
}
