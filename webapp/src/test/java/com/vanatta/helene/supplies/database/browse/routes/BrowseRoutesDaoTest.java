package com.vanatta.helene.supplies.database.browse.routes;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.*;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.manage.inventory.InventoryDao;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrowseRoutesDaoTest {

  String newSiteWithNeed;
  long hasNeedSiteId;

  String newSiteWithInventory;
  long newSiteWithInventoryId;

  @BeforeEach
  void setup() {
    TestConfiguration.setupDatabase();

    String clean =
        """
        delete from site_distance_matrix;
        delete from delivery_item;
        delete from delivery_confirmation;
        delete from delivery;
        delete from site_item_audit;
        delete from site_item;
        delete from item_tag;
        delete from site_audit_trail;
        delete from additional_site_manager;
        delete from site;
        """;
    jdbiTest.withHandle(h -> h.createScript(clean).execute());

    /* From Site */
    newSiteWithNeed = TestConfiguration.addSite("needs");
    hasNeedSiteId = TestConfiguration.getSiteId(newSiteWithNeed);
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "gloves", ItemStatus.URGENTLY_NEEDED.getText());
    ManageSiteDao.updateSiteField(
        jdbiTest, hasNeedSiteId, ManageSiteDao.SiteField.SITE_HOURS, "M-F");

    /* To Site */
    newSiteWithInventory = TestConfiguration.addSite("warehouse");
    newSiteWithInventoryId = TestConfiguration.getSiteId(newSiteWithInventory);
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "gloves", ItemStatus.OVERSUPPLY.getText());
    ManageSiteDao.updateSiteField(
        jdbiTest, newSiteWithInventoryId, ManageSiteDao.SiteField.SITE_HOURS, "W-F");
  }

  /**
   * Set up a site with inventory, another with needs - validate that the site pairing are in the
   * results for needs mathing.
   */
  @Test
  void validateNeedsQuery() {
    var results = BrowseRoutesDao.findDeliveryOptions(jdbiTest, null, null, List.of("NC"));

    Assertions.assertDoesNotThrow(
        () ->
            results.stream()
                .filter(r -> r.getFromSiteName().equals(newSiteWithInventory))
                .filter(r -> r.getToSiteName().equals(newSiteWithNeed))
                .filter(r -> r.getFromHours().equals("W-F"))
                .filter(r -> r.getToHours().equals("M-F"))
                .findAny()
                .orElseThrow(),
        String.format(
            "Expecting to have from-site: %s, going to to-site: %s,\nresults were: %s",
            newSiteWithInventory, newSiteWithNeed, results));
  }

  @Test
  void fetchSites() {
    TestConfiguration.setupDatabase();

    var results = BrowseRoutesDao.fetchSites(jdbiTest, List.of("NC"));
    assertThat(results).isNotEmpty();
    results.forEach(
        r -> {
          assertThat(r.getWssId()).isNotEqualTo(0L);
          assertThat(r.getSiteName()).isNotNull();
        });
  }

  @Test
  void fetchDeliveryOptionsWithCounty() {
    var results = BrowseRoutesDao.findDeliveryOptions(jdbiTest, null, "Watauga,NC", List.of("NC"));
    assertThat(results).isNotEmpty();
    results.forEach(
        r ->
            assertThat(r.getToCounty().equals("Watauga") || r.getFromCounty().equals("Watauaga"))
                .isTrue());

    results = BrowseRoutesDao.findDeliveryOptions(jdbiTest, null, "Polk,TN", List.of("NC", "TN"));
    assertThat(results).isEmpty();
  }

  /** Make sure that the item WSS-ID values are all set in return results of delivery options. */
  @Test
  void deliveryOptions_returnsItemWssIdsThatAreSet() {
    var results = BrowseRoutesDao.findDeliveryOptions(jdbiTest, null, null, List.of("NC"));
    assertThat(results).isNotEmpty();
    results.forEach(r -> assertThat(r.getItemWssIds()).doesNotContain(0L));
  }
}
