package com.vanatta.helene.supplies.database.manage.add.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao.SiteDetailData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AddSiteDaoTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  private static final AddSiteData siteData1 =
      AddSiteData.builder()
          .siteName("site data 1")
          .contactNumber("555-555-5555")
          .website("www.website.com")
          .siteType(SiteType.DISTRIBUTION_CENTER)
          .streetAddress("data 1 address")
          .city("data 1 city")
          .county("Ashe")
          .state("NC")
          .maxSupplyLoad("Car")
          .contactNumber("111")
          .build();

  private static final AddSiteData siteData2 =
      AddSiteData.builder()
          .siteName("site data 2")
          .siteType(SiteType.SUPPLY_HUB)
          .streetAddress("data 2 address")
          .city("data 2 city")
          .county("Ashe")
          .state("NC")
          .maxSupplyLoad("Car")
          .contactNumber("000")
          .build();

  @Test
  void addSite() {
    AddSiteDao.addSite(TestConfiguration.jdbiTest, siteData1);

    long id = TestConfiguration.getSiteId(siteData1.getSiteName());
    SiteDetailData details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, id);

    assertThat(details.getContactNumber()).isEqualTo(siteData1.getContactNumber());
    assertThat(details.getWebsite()).isEqualTo(siteData1.getWebsite());
    assertThat(details.getSiteType()).isEqualTo(siteData1.getSiteType().getText());
    assertThat(details.getSiteName()).isEqualTo(siteData1.getSiteName());
    assertThat(details.getAddress()).isEqualTo(siteData1.getStreetAddress());
    assertThat(details.getCity()).isEqualTo(siteData1.getCity());
    assertThat(details.getCounty()).isEqualTo(siteData1.getCounty());
  }

  @Test
  void addSiteWithOnlyRequiredFields() {
    AddSiteDao.addSite(TestConfiguration.jdbiTest, siteData2);

    long id = TestConfiguration.getSiteId(siteData2.getSiteName());
    SiteDetailData details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, id);

    assertThat(details.getContactNumber()).isEqualTo("000");
    assertThat(details.getWebsite()).isNull();
    assertThat(details.getSiteType()).isEqualTo(siteData2.getSiteType().getText());
    assertThat(details.getSiteName()).isEqualTo(siteData2.getSiteName());
    assertThat(details.getAddress()).isEqualTo(siteData2.getStreetAddress());
    assertThat(details.getCity()).isEqualTo(siteData2.getCity());
    assertThat(details.getCounty()).isEqualTo(siteData2.getCounty());
  }

  @Nested
  class InsertFailCases {

    @Test
    void addDuplicateShouldFail() {
      AddSiteDao.addSite(
          TestConfiguration.jdbiTest, siteData2.toBuilder().siteName("duplicate").build());

      assertThrows(
          AddSiteDao.DuplicateSiteException.class,
          () ->
              AddSiteDao.addSite(
                  TestConfiguration.jdbiTest, siteData2.toBuilder().siteName("duplicate").build()));
    }

    @Test
    void addSiteWithBadCountyShouldFail() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              AddSiteDao.addSite(
                  TestConfiguration.jdbiTest,
                  siteData2.toBuilder().county("invalid-county").build()));
    }
  }
}
