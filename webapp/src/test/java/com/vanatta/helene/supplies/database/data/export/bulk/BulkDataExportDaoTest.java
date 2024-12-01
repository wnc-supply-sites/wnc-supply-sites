package com.vanatta.helene.supplies.database.data.export.bulk;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.export.bulk.BulkDataExportDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BulkDataExportDaoTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void queriesForBulkSync() {
    assertThat(BulkDataExportDao.fetchAllSites(TestConfiguration.jdbiTest)).isNotEmpty();
    assertThat(BulkDataExportDao.getAllItems(TestConfiguration.jdbiTest)).isNotEmpty();
  }
}
