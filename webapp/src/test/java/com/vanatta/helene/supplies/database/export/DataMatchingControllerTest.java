package com.vanatta.helene.supplies.database.export;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DataMatchingControllerTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
    ;
  }

  @Test
  void runQuery() {
    var result = DataMatchingController.execute(TestConfiguration.jdbiTest, -200);

    assertThat(result).isNotNull();

    var firstResult = result.getFirst();
    assertThat(firstResult.getAirtableId()).isNotNull();
    assertThat(firstResult.getWssId()).isNotNull();
    assertThat(firstResult.getSiteName()).isNotNull();
    assertThat(firstResult.getSiteAddress()).isNotNull();
    assertThat(firstResult.getCity()).isNotNull();
    assertThat(firstResult.getCounty()).isNotNull();
    assertThat(firstResult.getState()).isNotNull();
    assertThat(firstResult.getItemName()).isNotNull();
    assertThat(firstResult.getOverlapCount()).isNotNull();
  }
}
