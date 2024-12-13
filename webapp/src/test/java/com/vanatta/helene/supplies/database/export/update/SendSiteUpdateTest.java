package com.vanatta.helene.supplies.database.export.update;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.DonationStatus;
import com.vanatta.helene.supplies.database.data.SiteType;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SendSiteUpdateTest {

  @BeforeAll
  static void setUp() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void fetchWssId() {
    // should fetch the wss ID of 'site1', based on data inserted in 'TestData.sql'
    var result =
        SendSiteUpdate.fetchWssIdByAirtableId(
            TestConfiguration.jdbiTest, TestConfiguration.SITE1_AIRTABLE_ID);
    assertThat(result).isEqualTo(TestConfiguration.SITE1_WSS_ID);
  }

  /**
   * For each site, we should be able to return some data, even if it is empty (should be non-null &
   * throw no errors.)
   */
  @ParameterizedTest
  @ValueSource(strings = {"site1", "site2", "site3", "site4"})
  void queriesForSiteSync(String siteName) {
    var siteDataResult =
        SendSiteUpdate.lookupSite(
            TestConfiguration.jdbiTest, TestConfiguration.getSiteId(siteName));
    assertThat(siteDataResult).isNotNull();
    assertThat(siteDataResult.getSiteName()).isNotNull();
    assertThat(siteDataResult.getSiteTypes()).isNotEmpty();
  }

  /**
   * Site1 should fill out all data fields. We can make sure we are loading them all and sending
   * them out as a JSON.
   */
  @Test
  void checkSiteData() {
    var siteDataResult =
        SendSiteUpdate.lookupSite(TestConfiguration.jdbiTest, TestConfiguration.getSiteId("site1"));
    assertThat(siteDataResult.getWssId()).isNotNull();
    assertThat(siteDataResult.getAirtableId()).isNotNull();
    assertThat(siteDataResult.getSiteName()).isNotNull();
    assertThat(siteDataResult.getSiteTypes()).contains("POD", "POC");
    assertThat(siteDataResult.getContactNumber()).isNotNull();
    assertThat(siteDataResult.getContactEmail()).isNotNull();
    assertThat(siteDataResult.getContactName()).isNotNull();
    assertThat(siteDataResult.getAddress()).isNotNull();
    assertThat(siteDataResult.getCity()).isNotNull();
    assertThat(siteDataResult.getState()).isNotNull();
    assertThat(siteDataResult.getCounty()).isNotNull();
    assertThat(siteDataResult.getWebsite()).isNotNull();
    assertThat(siteDataResult.getFacebook()).isNotNull();
    assertThat(siteDataResult.getDonationStatus()).isNotNull();
    assertThat(siteDataResult.isActive()).isTrue();
    assertThat(siteDataResult.isPubliclyVisible()).isTrue();
    assertThat(siteDataResult.getHours()).isNotNull();
    assertThat(siteDataResult.getMaxSupplyTruckSize()).isNotNull();
    assertThat(siteDataResult.isHasForkLift()).isNotNull();
    assertThat(siteDataResult.isHasIndoorStorage()).isNotNull();
    assertThat(siteDataResult.isHasLoadingDock()).isNotNull();
    
    assertThat(siteDataResult.isOnboarded()).isNotNull();
    assertThat(siteDataResult.getInactiveReason()).isNotNull();
  }

  @Builder
  @Value
  static class SiteTypeDataConversionScenario {
    SendSiteUpdate.SiteExportDataResult input;
    List<String> expectedSiteType;
  }

  /**
   * Validates that we create the correct 'site-type' list based on site type and whether the site
   * is giving or receiving supplies.
   */
  @ParameterizedTest
  @MethodSource
  void checkDataConversionToSiteType(SiteTypeDataConversionScenario scenario) {
    SendSiteUpdate.SiteExportJson result = new SendSiteUpdate.SiteExportJson(scenario.input);
    assertThat(result.getSiteTypes())
        .containsOnly(scenario.expectedSiteType.toArray(String[]::new));
  }

  private static final SendSiteUpdate.SiteExportDataResult dbDataSample =
      SendSiteUpdate.SiteExportDataResult.builder()
          .siteType(SiteType.DISTRIBUTION_CENTER.getText())
          .build();

  static List<SiteTypeDataConversionScenario> checkDataConversionToSiteType() {

    return List.of(
        SiteTypeDataConversionScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .acceptingDonations(true)
                    .distributingSupplies(false)
                    .build())
            .expectedSiteType(List.of("POC"))
            .build(),
        SiteTypeDataConversionScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .siteType(SiteType.DISTRIBUTION_CENTER.getText())
                    .acceptingDonations(false)
                    .distributingSupplies(true)
                    .build())
            .expectedSiteType(List.of("POD"))
            .build(),
        SiteTypeDataConversionScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .siteType(SiteType.DISTRIBUTION_CENTER.getText())
                    .acceptingDonations(true)
                    .distributingSupplies(true)
                    .build())
            .expectedSiteType(List.of("POC", "POD"))
            .build(),
        SiteTypeDataConversionScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .siteType(SiteType.SUPPLY_HUB.getText())
                    .acceptingDonations(true)
                    .distributingSupplies(true)
                    .build())
            .expectedSiteType(List.of("POC", "POD", "HUB"))
            .build(),
        SiteTypeDataConversionScenario.builder()
            .input(
                dbDataSample.toBuilder()
                    .siteType(SiteType.SUPPLY_HUB.getText())
                    .acceptingDonations(true)
                    .distributingSupplies(false)
                    .build())
            .expectedSiteType(List.of("POC", "HUB"))
            .build(),
        SiteTypeDataConversionScenario.builder()
            .input(
                dbDataSample.toBuilder()
                    .siteType(SiteType.SUPPLY_HUB.getText())
                    .acceptingDonations(false)
                    .distributingSupplies(true)
                    .build())
            .expectedSiteType(List.of("POD", "HUB"))
            .build());
  }

  @Builder
  @Value
  static class DonationStatusScenario {
    SendSiteUpdate.SiteExportDataResult input;
    DonationStatus expectedStatus;
  }

  @ParameterizedTest
  @MethodSource
  void donationStatus(DonationStatusScenario scenario) {
    SendSiteUpdate.SiteExportJson result = new SendSiteUpdate.SiteExportJson(scenario.input);
    assertThat(result.getDonationStatus()).isEqualTo(scenario.expectedStatus.getTextValue());
  }

  static List<DonationStatusScenario> donationStatus() {
    return List.of(
        DonationStatusScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .active(true)
                    .acceptingDonations(true)
                    .build())
            .expectedStatus(DonationStatus.ACCEPTING_DONATIONS)
            .build(),
        DonationStatusScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .active(true)
                    .acceptingDonations(false)
                    .build())
            .expectedStatus(DonationStatus.NOT_ACCEPTING_DONATIONS)
            .build(),
        DonationStatusScenario.builder()
            .input(
                dbDataSample.toBuilder() //
                    .active(false)
                    .build())
            .expectedStatus(DonationStatus.CLOSED)
            .build());
  }
}
