package com.vanatta.helene.supplies.database.export.update;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SendSiteUpdateTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  /**
   * For each site, we should be able to return some data, even if it is empty (should be non-null &
   * throw no errors.)
   */
  @ParameterizedTest
  @ValueSource(strings = {"site1", "site2", "site3", "site4"})
  void queriesForSiteSync(String siteName) {
    var siteDataResult =
        SendSiteUpdate.lookupSite(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId(siteName));
    assertThat(siteDataResult).isNotNull();
    assertThat(siteDataResult.getSiteName()).isNotNull();
    assertThat(siteDataResult.getSiteType()).isNotEmpty();
  }
}
