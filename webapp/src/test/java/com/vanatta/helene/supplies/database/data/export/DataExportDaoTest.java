package com.vanatta.helene.supplies.database.data.export;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DataExportDaoTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void testQueries() {
    DataExportDao.fetchAllSites(TestConfiguration.jdbiTest);
    DataExportDao.fetchAllSiteItems(TestConfiguration.jdbiTest);
    DataExportDao.lookupSite(TestConfiguration.jdbiTest, TestConfiguration.getSiteId());
  }
}
