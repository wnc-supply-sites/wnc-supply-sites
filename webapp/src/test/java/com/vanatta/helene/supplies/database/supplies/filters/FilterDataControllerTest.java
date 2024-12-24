package com.vanatta.helene.supplies.database.supplies.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FilterDataControllerTest {

  private static final Jdbi jdbiTest = TestConfiguration.jdbiTest;
  private final FilterDataController filterDataController =
      new FilterDataController(
          jdbiTest, new CookieAuthenticator(TestConfiguration.jdbiTest, false));

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void counties() {
    var response = filterDataController.getFilterData();

    // spot check we return a few expected values, counties of sites that are known to be active
    assertThat(response.getCounties()).contains("Buncombe", "Watauga");
  }

  @Test
  void items() {
    var response = filterDataController.getFilterData();

    // spot check we return a few expected values
    assertThat(response.getItems()).contains("water", "new clothes", "gloves");
  }

  @Test
  void sites() {
    var response = filterDataController.getFilterData();

    // site3 is not active
    assertThat(response.getSites()).doesNotContain("site3");
    // all active sites should be returned
    assertThat(response.getSites()).contains("site1", "site4", "site5", "site6");
  }

  /** Validate that only authenticated users can see site listing for private sites. */
  @Test
  void siteListAndAuthentication() {
    String siteName = TestConfiguration.addSite();
    long siteId = TestConfiguration.getSiteId(siteName);
    ManageSiteDao.updateSitePubliclyVisible(TestConfiguration.jdbiTest, siteId, false);

    var response = filterDataController.getFilterData(AuthenticatedMode.AUTHENTICATED);
    assertThat(response.getSites()).contains(siteName);

    //  site should drop off the filter list if user is not authenticated
    response = filterDataController.getFilterData(AuthenticatedMode.NOT_AUTHENTICATED);
    assertThat(response.getSites()).doesNotContain(siteName);
  }

  /**
   * Only authenticated users can see all sites. If a private-only site is the only one in a county,
   * then unauthenticcated users should not see that county.
   */
  @Test
  void countyListAndAuthentication() {
    // create a unique county for our site that will be private
    String siteName = TestConfiguration.addSite();
    long siteId = TestConfiguration.getSiteId(siteName);
    ManageSiteDao.updateSitePubliclyVisible(TestConfiguration.jdbiTest, siteId, false);
    TestConfiguration.addCounty("unique", "AA");
    ManageSiteDao.updateSiteField(
        TestConfiguration.jdbiTest, siteId, ManageSiteDao.SiteField.COUNTY, "unique,AA");

    var response = filterDataController.getFilterData(AuthenticatedMode.AUTHENTICATED);
    assertThat(response.getCounties()).contains("unique");

    //  site should drop off the filter list if user is not authenticated
    response = filterDataController.getFilterData(AuthenticatedMode.NOT_AUTHENTICATED);
    assertThat(response.getCounties()).doesNotContain("unique");
  }
}
