package com.vanatta.helene.supplies.database.manage.receiving;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.util.HtmlSelectOptionsUtil;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SiteReceivingControllerTest {
  SiteReceivingController siteReceivingController =
      new SiteReceivingController(TestConfiguration.jdbiTest, SendSiteUpdate.newDisabled());

  @BeforeAll
  static void setupDb() {
    TestConfiguration.setupDatabase();
  }

  /** For a known site, validates that we populate all the page params. */
  @ParameterizedTest
  @EnumSource(SiteReceivingController.PageParam.class)
  void showPageParams(SiteReceivingController.PageParam param) {
    // site1 should have every field populated.
    long siteId = TestConfiguration.getSiteId("site1");

    var response = siteReceivingController.showSiteContactPage(String.valueOf(siteId));
    assertThat(response.getModelMap()).containsKey(param.text);
    assertThat(response.getModelMap().get(param.text)).isNotNull();
  }

  /** Sets a max supply value, gets page params and validates that the correct one is selected. */
  @Test
  void correctMaxSupplySelected() {
    long siteId = TestConfiguration.getSiteId("site1");
    ManageSiteDao.updateMaxSupply(TestConfiguration.jdbiTest, siteId, "Car");

    var response = siteReceivingController.showSiteContactPage(String.valueOf(siteId));

    var items =
        (List<HtmlSelectOptionsUtil.ItemListing>)
            response
                .getModelMap()
                .getAttribute(SiteReceivingController.PageParam.MAX_SUPPLY_OPTIONS.text);
    assertThat(items)
        .contains(
            HtmlSelectOptionsUtil.ItemListing.builder().name("Car").selected("selected").build());
  }

  @Test
  void updateSiteReceiving() {
    long siteId = TestConfiguration.getSiteId();

    Map<String, String> yesToAll =
        Map.of(
            SiteReceivingController.SiteReceivingParam.SITE_ID.text, String.valueOf(siteId),
            SiteReceivingController.SiteReceivingParam.HAS_FORKLIFT.text, String.valueOf(true),
            SiteReceivingController.SiteReceivingParam.HAS_LOADING_DOCK.text, String.valueOf(true),
            SiteReceivingController.SiteReceivingParam.HAS_INDOOR_STORAGE.text,
                String.valueOf(true));

    var response = siteReceivingController.updateSiteReceiving(yesToAll);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
    assertThat(details.isHasForklift()).isTrue();
    assertThat(details.isHasLoadingDock()).isTrue();
    assertThat(details.isHasIndoorStorage()).isTrue();

    Map<String, String> noToAll =
        Map.of(
            SiteReceivingController.SiteReceivingParam.SITE_ID.text, String.valueOf(siteId),
            SiteReceivingController.SiteReceivingParam.HAS_FORKLIFT.text, String.valueOf(false),
            SiteReceivingController.SiteReceivingParam.HAS_LOADING_DOCK.text, String.valueOf(false),
            SiteReceivingController.SiteReceivingParam.HAS_INDOOR_STORAGE.text,
                String.valueOf(false));

    response = siteReceivingController.updateSiteReceiving(noToAll);
    details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(details.isHasForklift()).isFalse();
    assertThat(details.isHasLoadingDock()).isFalse();
    assertThat(details.isHasIndoorStorage()).isFalse();
  }
}
