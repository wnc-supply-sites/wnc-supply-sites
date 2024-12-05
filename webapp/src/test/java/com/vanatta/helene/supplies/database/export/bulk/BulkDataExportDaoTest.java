package com.vanatta.helene.supplies.database.export.bulk;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BulkDataExportDaoTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void queriesForBulkSync() {
    assertThat(BulkDataExportDao.getAllItems(TestConfiguration.jdbiTest)).isNotEmpty();
    assertThat(BulkDataExportDao.fetchAllSites(TestConfiguration.jdbiTest)).isNotEmpty();
  }

  /**
   * We have exactly one dispatch request in the test data, retrieve it, and validate its contents.
   */
  @Test
  void getDispatchRequest() {
    var needRequest = BulkDataExportDao.getAllNeedsRequests(TestConfiguration.jdbiTest).getFirst();

    assertThat(needRequest.getNeedRequestId()).isEqualTo("#1");
    assertThat(needRequest.getSite()).isEqualTo("site6");
    assertThat(needRequest.getStatus()).isEqualTo("NEW");
    assertThat(needRequest.getSuppliesNeeded()).contains("water", "used clothes");
    assertThat(needRequest.getSuppliesUrgentlyNeeded()).contains("gloves");
  }
}
