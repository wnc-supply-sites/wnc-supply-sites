package com.vanatta.helene.supplies.database.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class SiteContactControllerTest {
  SiteContactController siteContactController =
      new SiteContactController(TestConfiguration.jdbiTest, SendSiteUpdate.newDisabled());

  /** Make sure that the county listing displayed has the correct county selected for a given site */
  @Test
  void manageContactSelectsCorrectCounty() {
    long siteId = TestConfiguration.getSiteId();
    SiteDetailDao.SiteDetailData siteDetailData =
        SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);

    ModelAndView result = siteContactController.showSiteContactPage(String.valueOf(siteId));

    List<SiteContactController.ItemListing> countyListResult =
        (List<SiteContactController.ItemListing>)
            result.getModelMap().get(SiteContactController.COUNTY_LIST);

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

  /** Make sure state list has the correct state selected for a given site */
  @Test
  void manageContactSelectsCorrectState() {
    long siteId = TestConfiguration.getSiteId();
    SiteDetailDao.SiteDetailData siteDetailData =
        SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);

    ModelAndView result = siteContactController.showSiteContactPage(String.valueOf(siteId));

    List<SiteContactController.ItemListing> stateListing =
        (List<SiteContactController.ItemListing>)
            result.getModelMap().get(SiteContactController.STATE_LIST);

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
}
