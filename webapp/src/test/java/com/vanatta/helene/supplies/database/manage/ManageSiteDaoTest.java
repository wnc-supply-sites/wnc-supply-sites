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
    return getSiteId("site1");
  }

  static long getSiteId(String siteName) {
    return TestConfiguration.jdbiTest.withHandle(
        handle ->
            handle
                .createQuery("select id from site where name = '" + siteName + "'")
                .mapTo(Long.class)
                .one());
  }

  @Test
  void fetchSiteName() {
    long siteId = getSiteId();

    String result = ManageSiteDao.fetchSiteName(TestConfiguration.jdbiTest, siteId);

    assertThat(result).isEqualTo("site1");
  }

  @Test
  void siteStatusActive() {
    long siteId = getSiteId("site1");
    var result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isActive()).isTrue();

    ManageSiteDao.updateSiteActiveFlag(TestConfiguration.jdbiTest, siteId, false);
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isActive()).isFalse();

    ManageSiteDao.updateSiteActiveFlag(TestConfiguration.jdbiTest, siteId, true);
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isActive()).isTrue();

    siteId = getSiteId("site2");
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isActive()).isTrue();

    siteId = getSiteId("site3");
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isActive()).isFalse();
  }

  @Test
  void setStatusAcceptingDonations() {
    long siteId = getSiteId("site1");
    var result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isAcceptingDonations()).isTrue();

    ManageSiteDao.updateSiteAcceptingDonationsFlag(TestConfiguration.jdbiTest, siteId, false);
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isAcceptingDonations()).isFalse();

    ManageSiteDao.updateSiteAcceptingDonationsFlag(TestConfiguration.jdbiTest, siteId, true);
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isAcceptingDonations()).isTrue();

    siteId = getSiteId("site2");
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isAcceptingDonations()).isFalse();

    siteId = getSiteId("site3");
    result = ManageSiteDao.fetchSiteStatus(TestConfiguration.jdbiTest, siteId);
    assertThat(result.isAcceptingDonations()).isTrue();
  }

  @Test
  void fetchSiteInventory() {
    long siteId = getSiteId("site1");
    var result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);

    ManageSiteDao.SiteInventory water =
        result.stream()
            .filter(r -> r.getItemName().equalsIgnoreCase("water"))
            .findAny()
            .orElseThrow();
    assertThat(water.isActive()).isTrue();
    assertThat(water.getItemStatus()).isEqualTo("Requested");

    ManageSiteDao.SiteInventory clothes =
        result.stream()
            .filter(r -> r.getItemName().equalsIgnoreCase("new clothes"))
            .findAny()
            .orElseThrow();
    assertThat(clothes.isActive()).isTrue();
    assertThat(clothes.getItemStatus()).isEqualTo("Urgent Need");

    ManageSiteDao.SiteInventory usedClothes =
        result.stream()
            .filter(r -> r.getItemName().equalsIgnoreCase("used clothes"))
            .findAny()
            .orElseThrow();
    assertThat(usedClothes.isActive()).isTrue();
    assertThat(usedClothes.getItemStatus()).isEqualTo("Oversupply");

    ManageSiteDao.SiteInventory gloves =
        result.stream()
            .filter(r -> r.getItemName().equalsIgnoreCase("gloves"))
            .findAny()
            .orElseThrow();
    assertThat(gloves.isActive()).isFalse();
    assertThat(gloves.getItemStatus()).isNull();

    ManageSiteDao.SiteInventory randomStuff =
        result.stream()
            .filter(r -> r.getItemName().equalsIgnoreCase("random stuff"))
            .findAny()
            .orElseThrow();
    assertThat(randomStuff.isActive()).isFalse();
    assertThat(randomStuff.getItemStatus()).isNull();
  }
}
