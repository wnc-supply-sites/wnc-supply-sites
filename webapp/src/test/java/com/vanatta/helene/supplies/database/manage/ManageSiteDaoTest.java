package com.vanatta.helene.supplies.database.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import java.util.List;
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

    ManageSiteDao.SiteInventory water = findItemByName(result, "water");
    assertThat(water.isActive()).isTrue();
    assertThat(water.getItemStatus()).isEqualTo("Requested");

    ManageSiteDao.SiteInventory clothes = findItemByName(result, "new clothes");
    assertThat(clothes.isActive()).isTrue();
    assertThat(clothes.getItemStatus()).isEqualTo("Urgent Need");

    ManageSiteDao.SiteInventory usedClothes = findItemByName(result, "used clothes");
    assertThat(usedClothes.isActive()).isTrue();
    assertThat(usedClothes.getItemStatus()).isEqualTo("Oversupply");

    ManageSiteDao.SiteInventory gloves = findItemByName(result, "gloves");
    assertThat(gloves.isActive()).isFalse();
    assertThat(gloves.getItemStatus()).isNull();

    ManageSiteDao.SiteInventory randomStuff = findItemByName(result, "random stuff");
    assertThat(randomStuff.isActive()).isFalse();
    assertThat(randomStuff.getItemStatus()).isNull();
  }

  private static ManageSiteDao.SiteInventory findItemByName(
      List<ManageSiteDao.SiteInventory> items, String itemName) {
    return items.stream()
        .filter(r -> r.getItemName().equalsIgnoreCase(itemName))
        .findAny()
        .orElseThrow();
  }

  @Test
  void updateSiteItemActive() {
    long siteId = getSiteId("site1");

    // first make sure 'gloves' are not active
    var result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    ManageSiteDao.SiteInventory gloves = findItemByName(result, "gloves");
    assertThat(gloves.isActive()).isFalse();

    // set gloves to back to 'active'
    ManageSiteDao.updateSiteItemActive(TestConfiguration.jdbiTest, siteId, "gloves", "Oversupply");

    // verify gloves are active
    result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    gloves = findItemByName(result, "gloves");
    assertThat(gloves.isActive()).isTrue();

    // set gloves to back to 'inactive'
    ManageSiteDao.updateSiteItemInactive(TestConfiguration.jdbiTest, siteId, "gloves");

    // verify gloves are inactive
    result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    gloves = findItemByName(result, "gloves");
    assertThat(gloves.isActive()).isFalse();
  }

  @Test
  void updateSiteItemStatus() {
    long siteId = getSiteId("site1");

    // validate gloves status is 'Requested'
    var result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    ManageSiteDao.SiteInventory water = findItemByName(result, "water");
    assertThat(water.getItemStatus()).isEqualTo("Requested");

    // change gloves status to 'Urgent Need'
    ManageSiteDao.updateItemStatus(TestConfiguration.jdbiTest, siteId, "water", "Urgent Need");

    // validate gloves status is updated 'Urgent Need'
    result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    water = findItemByName(result, "water");
    assertThat(water.getItemStatus()).isEqualTo("Urgent Need");

    // change gloves status to 'Oversupply'
    ManageSiteDao.updateItemStatus(TestConfiguration.jdbiTest, siteId, "water", "Oversupply");

    // validate gloves status is updated 'Oversupply'
    result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    water = findItemByName(result, "water");
    assertThat(water.getItemStatus()).isEqualTo("Oversupply");

    // change gloves status back to 'Requested'
    ManageSiteDao.updateItemStatus(TestConfiguration.jdbiTest, siteId, "water", "Requested");

    // validate gloves status is updated 'Requested'
    result = ManageSiteDao.fetchSiteInventory(TestConfiguration.jdbiTest, siteId);
    water = findItemByName(result, "water");
    assertThat(water.getItemStatus()).isEqualTo("Requested");
  }

  @Test
  void addItem() {
    int itemCountPreInsert = countItems();

    boolean result = ManageSiteDao.addNewItem(TestConfiguration.jdbiTest, "new item");
    assertThat(result).isTrue();

    int itemCountPostInsert = countItems();
    assertThat(itemCountPreInsert + 1).isEqualTo(itemCountPostInsert);
  }

  private static int countItems() {
    return TestConfiguration.jdbiTest.withHandle(handle ->
        handle.createQuery("select count(*) from item")
            .mapTo(Integer.class)
            .one());
  }

  @Test
  void duplicateItemCannotBeAdded() {
    ManageSiteDao.addNewItem(TestConfiguration.jdbiTest, "some item");
    boolean result = ManageSiteDao.addNewItem(TestConfiguration.jdbiTest, "SOME ITEM");
    assertThat(result).isFalse();
  }
}
