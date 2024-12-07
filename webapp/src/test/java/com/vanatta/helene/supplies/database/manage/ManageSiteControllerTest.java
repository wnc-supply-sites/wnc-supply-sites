package com.vanatta.helene.supplies.database.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteController.ItemListing;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao.SiteDetailData;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class ManageSiteControllerTest {
  // TODO: test is broken, instead of null - use dummy values.. or put a config flag to disable..
  ManageSiteController manageSiteController =
      new ManageSiteController(TestConfiguration.jdbiTest, null);

  @Test
  void manageContactSelectsCorrectCounty() {
    long siteId = TestConfiguration.getSiteId();
    SiteDetailData siteDetailData =
        SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);

    ModelAndView result = manageSiteController.showSiteContactPage(String.valueOf(siteId));

    List<ItemListing> countyListResult =
        (List<ItemListing>) result.getModelMap().get(ManageSiteController.COUNTY_LIST);

    // validate that all listings are populated with non-null data
    countyListResult.forEach(
        listing -> {
          assertThat(listing.getName()).isNotNull();
          assertThat(listing.getName()).isNotBlank();
          // all values for 'selected' should eitehr be blank or 'selected'
          assertThat(listing.getSelected()).isNotNull();
          assertThat(listing.getSelected()).isIn("", "selected");
        });

    // find the listing for the county of the site, it should be selected
    var listing =
        countyListResult.stream()
            .filter(f -> f.getName().equals(siteDetailData.getCounty()))
            .findAny()
            .orElseThrow();
    assertThat(listing.getSelected()).isEqualTo("selected");

    // find all other county listings, they should not be selected.
    countyListResult.stream()
        .filter(f -> !f.getName().equals(siteDetailData.getCounty()))
        .forEach(r -> assertThat(r.getSelected()).isEmpty());
  }

  @Test
  void manageContactSelectsCorrectState() {
    long siteId = TestConfiguration.getSiteId();
    SiteDetailData siteDetailData =
        SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);

    ModelAndView result = manageSiteController.showSiteContactPage(String.valueOf(siteId));

    List<ItemListing> stateListing =
        (List<ItemListing>) result.getModelMap().get(ManageSiteController.STATE_LIST);

    // validate that all listings are populated with non-null data
    stateListing.forEach(
        listing -> {
          assertThat(listing.getName()).isNotNull();
          assertThat(listing.getName()).isNotBlank();
          // all values for 'selected' should eitehr be blank or 'selected'
          assertThat(listing.getSelected()).isNotNull();
          assertThat(listing.getSelected()).isIn("", "selected");
        });

    // find the listing for the state of the site, it should be selected
    var listing =
        stateListing.stream()
            .filter(f -> f.getName().equals(siteDetailData.getState()))
            .findAny()
            .orElseThrow();
    assertThat(listing.getSelected()).isEqualTo("selected");

    // find all other county listings, they should not be selected.
    stateListing.stream()
        .filter(f -> !f.getName().equals(siteDetailData.getState()))
        .forEach(r -> assertThat(r.getSelected()).isEmpty());
  }

  @Nested
  class UpdateStatus {
    ManageSiteController manageSiteController =
        new ManageSiteController(TestConfiguration.jdbiTest, SendSiteUpdate.newDisabled());

    long siteId = TestConfiguration.getSiteId("site1");

    private void toggleFlag(ManageSiteController.EnumStatusUpdateFlag flag, boolean value) {
      manageSiteController.updateStatus(
          Map.of(
              "siteId",
              String.valueOf(siteId), //
              "statusFlag",
              flag.getText(),
              "newValue",
              String.valueOf(value)));
    }

    @Test
    void siteAcceptingSupplies() {
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.ACCEPTING_SUPPLIES, false);
      var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isAcceptingDonations()).isFalse();

      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.ACCEPTING_SUPPLIES, true);
      details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isAcceptingDonations()).isTrue();
    }

    @Test
    void siteDistributingSupplies() {
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.DISTRIBUTING_SUPPLIES, false);
      var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isDistributingSupplies()).isFalse();

      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.DISTRIBUTING_SUPPLIES, true);
      details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isDistributingSupplies()).isTrue();
    }

    @Test
    void siteType() {
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.SITE_TYPE, false);
      var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.getSiteType()).isEqualTo(SiteType.SUPPLY_HUB.getText());

      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.SITE_TYPE, true);
      details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.getSiteType()).isEqualTo(SiteType.DISTRIBUTION_CENTER.getText());
    }

    @Test
    void siteVisibleToPublic() {
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.PUBLICLY_VISIBLE, false);
      var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isPubliclyVisible()).isFalse();
      
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.PUBLICLY_VISIBLE, true);
      details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isPubliclyVisible()).isTrue();
    }

    @Test
    void siteActive() {
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.ACTIVE, false);
      var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isActive()).isFalse();
      
      toggleFlag(ManageSiteController.EnumStatusUpdateFlag.ACTIVE, true);
      details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
      assertThat(details.isActive()).isTrue();
    }
  }
}
