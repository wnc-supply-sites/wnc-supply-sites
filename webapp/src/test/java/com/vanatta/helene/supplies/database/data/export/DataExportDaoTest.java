package com.vanatta.helene.supplies.database.data.export;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.export.bulk.DataExportDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DataExportDaoTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void queriesForBulkSync() {
    assertThat(DataExportDao.fetchAllSites(TestConfiguration.jdbiTest)).isNotEmpty();
    assertThat(DataExportDao.fetchAllSiteItems(TestConfiguration.jdbiTest));
  }

  /**
   * For each site, we should be able to return some data, even if it is empty (should be non-null &
   * throw no errors.)
   */
  @ParameterizedTest
  @ValueSource(strings = {"site1", "site2", "site3", "site4"})
  void queriesForSiteSync(String siteName) {
    var siteDataResult =
        DataExportDao.lookupSite(TestConfiguration.jdbiTest, TestConfiguration.getSiteId(siteName));
    assertThat(siteDataResult).isNotNull();
    assertThat(siteDataResult.getSiteName()).isNotNull();
    assertThat(siteDataResult.getOldName()).isNotNull();

    var inventoryResult =
        DataExportDao.fetchAllSiteItemsForSite(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId(siteName));
    assertThat(inventoryResult).isNotNull();
    assertThat(inventoryResult.getSiteName()).isNotNull();
    assertThat(inventoryResult.getAvailable()).isNotNull();
    assertThat(inventoryResult.getNeeded()).isNotNull();
  }
}
