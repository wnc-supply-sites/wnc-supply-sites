package com.vanatta.helene.supplies.database.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.manage.ManageSiteController.CountyListing;
import com.vanatta.helene.supplies.database.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.site.details.SiteDetailDao.SiteDetailData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

class ManageSiteControllerTest {
  ManageSiteController manageSiteController =
      new ManageSiteController(
          TestConfiguration.jdbiTest, null); //new CookieAuthenticator(TestConfiguration.jdbiTest));

  @Test
  void manageContactSelectsCorrectCounty() {
    long siteId = TestConfiguration.getSiteId();
    SiteDetailData siteDetailData =
        SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);

    ModelAndView result = manageSiteController.manageContact(String.valueOf(siteId));

    List<CountyListing> countyListResult =
        (List<CountyListing>) result.getModelMap().get("countyList");

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
}
