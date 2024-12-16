package com.vanatta.helene.supplies.database.supplies.site.details;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SiteDetailControllerTest {

  /**
   * Validate that the site detail page contains all values from
   * 'SiteDetailController.TemplateParams'
   */
  @Test
  void renderSiteDetail() {
    long site1Id = TestConfiguration.getSiteId("site1");

    SiteDetailController siteDetailController =
        new SiteDetailController(
            TestConfiguration.jdbiTest, new CookieAuthenticator(TestConfiguration.jdbiTest));

    var model = siteDetailController.siteDetail(site1Id, null, null, true);

    assertThat(model.getModelMap().keySet())
        .containsAll(
            Arrays.stream(SiteDetailController.TemplateParams.values()).map(v -> v.text).toList());
  }
}
