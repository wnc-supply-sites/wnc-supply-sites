package com.vanatta.helene.supplies.database.browse.routes;

import static com.vanatta.helene.supplies.database.TestConfiguration.jdbiTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.manage.inventory.InventoryDao;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class BrowseRoutesControllerTest {

  @BeforeEach
  void setup() {
    TestConfiguration.setupDatabase();
    /* From Site */
    String newSiteWithNeed = TestConfiguration.addSite("needs");
    long hasNeedSiteId = TestConfiguration.getSiteId(newSiteWithNeed);

    // add quite a few items. This way the priority weighting will not filter out the delivery
    // option
    // if there are enough items
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "gloves", ItemStatus.URGENTLY_NEEDED.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "water", ItemStatus.URGENTLY_NEEDED.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "heater", ItemStatus.URGENTLY_NEEDED.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "batteries", ItemStatus.URGENTLY_NEEDED.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, hasNeedSiteId, "new clothes", ItemStatus.URGENTLY_NEEDED.getText());
    ManageSiteDao.updateSiteField(
        jdbiTest, hasNeedSiteId, ManageSiteDao.SiteField.SITE_HOURS, "M-F");

    /* To Site */
    String newSiteWithInventory = TestConfiguration.addSite("warehouse");
    long newSiteWithInventoryId = TestConfiguration.getSiteId(newSiteWithInventory);
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "gloves", ItemStatus.OVERSUPPLY.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "water", ItemStatus.OVERSUPPLY.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "heater", ItemStatus.OVERSUPPLY.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "batteries", ItemStatus.OVERSUPPLY.getText());
    InventoryDao.updateSiteItemActive(
        jdbiTest, newSiteWithInventoryId, "new clothes", ItemStatus.OVERSUPPLY.getText());

    ManageSiteDao.updateSiteField(
        jdbiTest, newSiteWithInventoryId, ManageSiteDao.SiteField.SITE_HOURS, "W-F");
  }

  /** Simple check that the browse routes page renders with all of its parameters. */
  @Test
  void validatePageRenders() {
    var controller = new BrowseRoutesController(TestConfiguration.jdbiTest, "");

    ModelAndView modelAndView = controller.browseRoutes(null, null, null);

    assertThat(modelAndView.getViewName()).isEqualTo("browse/routes");

    Arrays.stream(BrowseRoutesController.TemplateParams.values())
        .forEach(
            param ->
                assertThat(modelAndView.getModelMap().getAttribute(param.name()))
                    .describedAs(param.name())
                    .isNotNull());
    assertThat(
            (List<BrowseRoutesDao.DeliveryOption>)
                modelAndView
                    .getModelMap()
                    .getAttribute(BrowseRoutesController.TemplateParams.deliveryOptions.name()))
        .isNotEmpty();
  }

  @Test
  void filterByCounty() {}
}
