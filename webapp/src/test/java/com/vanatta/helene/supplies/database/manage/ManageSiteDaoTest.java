package com.vanatta.helene.supplies.database.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ManageSiteDaoTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void fetchSites() {
    var results = ManageSiteDao.fetchSiteList(TestConfiguration.jdbiTest);
    assertThat(results).isNotEmpty();

    results.forEach(result -> assertThat(result.getId()).isNotEqualTo(0L));
    var names =
        results.stream()
            .map(ManageSiteController.SiteSelection::getName)
            .collect(Collectors.toList());
    assertThat(names).contains("site1");
    names.forEach(name -> assertThat(name).isNotNull());
  }

  @Test
  void updateContact() {
    long siteId = getSiteId();

    // confirm contact number is null before we update it.
    assertThat(ManageSiteDao.fetchSiteContact(TestConfiguration.jdbiTest, siteId)).isNull();

    ManageSiteDao.updateSiteContact(TestConfiguration.jdbiTest, siteId, "999-596-111");

    assertThat(ManageSiteDao.fetchSiteContact(TestConfiguration.jdbiTest, siteId))
        .isEqualTo("999-596-111");
  }

  static long getSiteId() {
    return TestConfiguration.jdbiTest.withHandle(
        handle ->
            handle.createQuery("select id from site where name = 'site1'").mapTo(Long.class).one());
  }

  @Test
  void fetchSiteName() {
    long siteId = getSiteId();

    String result = ManageSiteDao.fetchSiteName(TestConfiguration.jdbiTest, siteId);

    assertThat(result).isEqualTo("site1");
  }
}
