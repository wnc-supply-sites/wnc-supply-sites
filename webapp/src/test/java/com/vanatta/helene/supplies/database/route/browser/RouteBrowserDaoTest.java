package com.vanatta.helene.supplies.database.route.browser;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.manage.inventory.InventoryDao;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RouteBrowserDaoTest {

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
  }

  /** Set up a site, validate that */
  @Test
  void validateNeedsQuery() {
    /* From Site */
    String newSiteWithNeed = TestConfiguration.addSite("needs");
    long hasNeedSiteId = TestConfiguration.getSiteId(newSiteWithNeed);
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "gloves", ItemStatus.URGENTLY_NEEDED.getText());
    ManageSiteDao.updateSiteField(jdbiTest, hasNeedSiteId, ManageSiteDao.SiteField.SITE_HOURS, "M-F");
    
    /* To Site */
    String newSiteWithInventory = TestConfiguration.addSite("warehouse");
    long newSiteWithInventoryId = TestConfiguration.getSiteId(newSiteWithInventory);
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "gloves", ItemStatus.OVERSUPPLY.getText());
    ManageSiteDao.updateSiteField(jdbiTest, newSiteWithInventoryId, ManageSiteDao.SiteField.SITE_HOURS, "W-F");
    
    
    var results = RouteBrowserDao.findDeliveryOptions(jdbiTest);

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
}
