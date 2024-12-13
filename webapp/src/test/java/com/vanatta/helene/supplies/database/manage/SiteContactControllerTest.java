package com.vanatta.helene.supplies.database.manage;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.servlet.ModelAndView;

class SiteContactControllerTest {
  SiteContactController siteContactController =
      new SiteContactController(TestConfiguration.jdbiTest, SendSiteUpdate.newDisabled());

  @BeforeAll
  static void setupDb() {
    TestConfiguration.setupDatabase();
  }

  /**
   * Loop through all fields that can be set to random data. Invoke the site update endpoint for a
   * quick end-to-end test, make sure simply that we get no errors.
   */
  @ParameterizedTest
  @MethodSource
  void updateSiteDataThrowsNoErrors(ManageSiteDao.SiteField field) {
    Map<String, String> params =
        Map.of(
            "siteId", String.valueOf(TestConfiguration.getSiteId()), //
            "field", field.getFrontEndName(),
            "newValue",
                field.getFrontEndName() + " " + UUID.randomUUID().toString().substring(0, 10));

    var response = siteContactController.updateSiteData(params);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
  }

  static List<ManageSiteDao.SiteField> updateSiteDataThrowsNoErrors() {
    // filter out fields that require specific values, any field
    // that cannot just be set to random data.
    return Arrays.asList(ManageSiteDao.SiteField.values()).stream()
        .filter(
            f ->
                f != ManageSiteDao.SiteField.STATE
                    && f != ManageSiteDao.SiteField.COUNTY
                    && f != ManageSiteDao.SiteField.SITE_NAME)
        .toList();
  }

  /**
   * Invoke site update endpoint for a specific field value. Validate that we truly update the
   * database.
   */
  @Test
  void updateSiteData_DoesUpdateDatabase() {
    long siteId = TestConfiguration.getSiteId();
    String newValue = "website" + UUID.randomUUID();
    Map<String, String> params =
        Map.of(
            "siteId", String.valueOf(siteId), //
            "field", ManageSiteDao.SiteField.WEBSITE.getFrontEndName(),
            "newValue", newValue);

    var response = siteContactController.updateSiteData(params);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    var siteLookup = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
    assertThat(siteLookup.getWebsite()).isEqualTo(newValue);
  }

  /** For a known site, validates that we populate all the page params. */
  @ParameterizedTest
  @EnumSource(SiteContactController.PageParam.class)
  void showPageParams(SiteContactController.PageParam param) {
    // site1 should have every field populated.
    long siteId = TestConfiguration.getSiteId("site1");
    Map<String, Object> params =
        SiteContactController.buildContactPageParams(TestConfiguration.jdbiTest, siteId)
            .orElseThrow();

    assertThat(params).containsKey(param.text);
    assertThat(params.get(param.text)).isNotNull();
  }

  /**
   * Make sure that the county listing displayed has the correct county selected for a given site
   */
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

  /** Sets a max supply value, gets page params and validates that the correct one is selected. */
  @Test
  void correctMaxSupplySelected() {
    long siteId = TestConfiguration.getSiteId("site1");
    ManageSiteDao.updateMaxSupply(TestConfiguration.jdbiTest, siteId, "Car");

    var response = siteContactController.showSiteContactPage(String.valueOf(siteId));

    var items =
        (List<SiteContactController.ItemListing>)
            response.getModelMap().get(SiteContactController.PageParam.MAX_SUPPLY_OPTIONS.text);
    assertThat(items)
        .contains(
            SiteContactController.ItemListing.builder().name("Car").selected("selected").build());
    assertThat(items)
        .contains(SiteContactController.ItemListing.builder().name("").selected(null).build());
  }
  
  @Test
  void updateSiteReceiving() {
    long siteId = TestConfiguration.getSiteId();
    
    Map<String,String> yesToAll = Map.of(
        SiteContactController.SiteReceivingParam.SITE_ID.text, String.valueOf(siteId),
        SiteContactController.SiteReceivingParam.HAS_FORKLIFT.text, String.valueOf(true),
        SiteContactController.SiteReceivingParam.HAS_LOADING_DOCK.text, String.valueOf(true),
        SiteContactController.SiteReceivingParam.HAS_INDOOR_STORAGE.text, String.valueOf(true)
    );
    
    var response = siteContactController.updateSiteReceiving(yesToAll);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    var details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
    assertThat(details.isHasForklift()).isTrue();
    assertThat(details.isHasLoadingDock()).isTrue();
    assertThat(details.isHasIndoorStorage()).isTrue();
    
    
    Map<String,String> noToAll = Map.of(
        SiteContactController.SiteReceivingParam.SITE_ID.text, String.valueOf(siteId),
        SiteContactController.SiteReceivingParam.HAS_FORKLIFT.text, String.valueOf(false),
        SiteContactController.SiteReceivingParam.HAS_LOADING_DOCK.text, String.valueOf(false),
        SiteContactController.SiteReceivingParam.HAS_INDOOR_STORAGE.text, String.valueOf(false)
        );
    
    response = siteContactController.updateSiteReceiving(noToAll);
    details = SiteDetailDao.lookupSiteById(TestConfiguration.jdbiTest, siteId);
    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(details.isHasForklift()).isFalse();
    assertThat(details.isHasLoadingDock()).isFalse();
    assertThat(details.isHasIndoorStorage()).isFalse();
  }
}
