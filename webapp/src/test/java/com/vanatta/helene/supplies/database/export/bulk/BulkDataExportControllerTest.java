package com.vanatta.helene.supplies.database.export.bulk;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BulkDataExportControllerTest {

  @BeforeAll
  static void dbSetup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void bulkDataExport() {
    var responseData =
        new BulkDataExportController(TestConfiguration.jdbiTest).exportData().getBody();

    assertThat(responseData.getItems()).isNotEmpty();
    assertThat(responseData.getSites()).isNotEmpty();
    assertThat(responseData.getNeedRequests()).isNotEmpty();
  }
}
