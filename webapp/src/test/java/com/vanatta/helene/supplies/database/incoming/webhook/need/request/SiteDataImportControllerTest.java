package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.SiteType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class SiteDataImportControllerTest {

  @BeforeAll
  static void setup() {
    TestConfiguration.setupDatabase();
  }

  private final SiteDataImportController.SiteUpdate sampleData =
      SiteDataImportController.SiteUpdate.builder()
          .airtableId(123L)
          .wssId(null) // wssId being null will be default make us insert data.
          .siteName("site-test " + UUID.randomUUID().toString())
          .siteType(List.of("HUB"))
          .build();

  private final SiteDataImportController siteDataImportController =
      new SiteDataImportController(TestConfiguration.jdbiTest);

  @Nested
  class Insert {
    private static final String selectSiteQuery =
        """
        select s.*, c.name county, c.state
        from site s
        join county c on c.id = s.county_id
        where s.airtable_id = :airtableId
        """;

    /** Insert a happy case amount, where incoming data is the full payload with every field set. */
    @Test
    void insert() {
      var input = sampleData;

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(200);
      // fetch results from DB and validate that everything is updated
      Map<String, Object> results =
          TestConfiguration.jdbiTest.withHandle(
              handle ->
                  handle
                      .createQuery(selectSiteQuery)
                      .bind("airtableId", input.getAirtableId())
                      .mapToMap()
                      .one());
      assertThat(results.get("name")).isEqualTo(input.getSiteName());
      assertThat(results.get("address")).isEqualTo(input.getStreetAddress());
      assertThat(results.get("city")).isEqualTo(input.getCity());
      assertThat(results.get("county")).isEqualTo(input.getCounty());
      assertThat(results.get("state")).isEqualTo(input.getState());
      assertThat(results.get("contact_number")).isEqualTo(input.getPhone());
      assertThat(results.get("website")).isEqualTo(input.getWebsite());
      assertThat(results.get("airtable_id")).isEqualTo(input.getAirtableId());
      assertThat(results.get("hours")).isEqualTo(input.getHours());
      assertThat(results.get("contact_name")).isEqualTo(input.getPointOfContact());
      assertThat(results.get("email")).isEqualTo(input.getEmail());
      assertThat(results.get("facebook")).isEqualTo(input.getFacebook());
    }

    /** Try to insert minimal data and validate we have a lot of null data. */
    @Test
    void insertMinimalData() {
      var input =
          sampleData.toBuilder()
              .pointOfContact(null)
              .website(null)
              .hours(null)
              .phone(null)
              .email(null)
              .facebook(null)
              .build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(200);
      // fetch results from DB and validate that everything is updated
      Map<String, Object> results =
          TestConfiguration.jdbiTest.withHandle(
              handle ->
                  handle
                      .createQuery(selectSiteQuery)
                      .bind("airtableId", input.getAirtableId())
                      .mapToMap()
                      .one());
      assertThat(results.get("name")).isEqualTo(input.getSiteName());
      assertThat(results.get("address")).isEqualTo(input.getStreetAddress());
      assertThat(results.get("city")).isEqualTo(input.getCity());
      assertThat(results.get("county")).isEqualTo(input.getCounty());
      assertThat(results.get("state")).isEqualTo(input.getState());
      assertThat(results.get("contact_number")).isNull();
      assertThat(results.get("website")).isNull();
      assertThat(results.get("airtable_id")).isEqualTo(input.getAirtableId());
      assertThat(results.get("hours")).isNull();
      assertThat(results.get("contact_name")).isNull();
      assertThat(results.get("email")).isNull();
      assertThat(results.get("facebook")).isNull();
    }

    @Builder
    @Value
    static class SiteTypeTestScenario {
      @NonNull List<String> inputSiteTypes;
      @NonNull String expectedSiteType;
    }

    @ParameterizedTest
    @MethodSource
    void siteType(SiteTypeTestScenario scenario) {
      var input = sampleData.toBuilder().siteType(scenario.inputSiteTypes).build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(200);
      String query =
          """
          select st.name
          from site s
          join site_type st on st.id = s.site_type_id
          where airtable_id = :airtableId
          """;
      String result =
          TestConfiguration.jdbiTest.withHandle(
              handle ->
                  handle
                      .createQuery(query)
                      .bind("airtableId", input.getAirtableId())
                      .mapTo(String.class)
                      .one());
      assertThat(result).isEqualTo(scenario.expectedSiteType);
    }

    static List<SiteTypeTestScenario> siteType() {
      return List.of(
          SiteTypeTestScenario.builder()
              .inputSiteTypes(List.of("HUB"))
              .expectedSiteType(SiteType.SUPPLY_HUB.getText())
              .build(),
          SiteTypeTestScenario.builder()
              .inputSiteTypes(List.of("POD"))
              .expectedSiteType(SiteType.DISTRIBUTION_CENTER.getText())
              .build(),
          SiteTypeTestScenario.builder()
              .inputSiteTypes(List.of("POD", "POC"))
              .expectedSiteType(SiteType.DISTRIBUTION_CENTER.getText())
              .build(),
          SiteTypeTestScenario.builder()
              .inputSiteTypes(List.of("HUB", "POD"))
              .expectedSiteType(SiteType.SUPPLY_HUB.getText())
              .build(),
          SiteTypeTestScenario.builder()
              .inputSiteTypes(List.of("HUB", "POC"))
              .expectedSiteType(SiteType.SUPPLY_HUB.getText())
              .build(),
          SiteTypeTestScenario.builder()
              .inputSiteTypes(List.of("HUB", "POD", "POC"))
              .expectedSiteType(SiteType.SUPPLY_HUB.getText())
              .build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "POC", "DNE"})
    void siteTypeIllegalValues(String illegalSiteType) {
      var input = sampleData.toBuilder().siteType(List.of(illegalSiteType)).build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void siteTypeEmptyValueIsIllegal() {
      var input = sampleData.toBuilder().siteType(List.of()).build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    /**
     * Expected data pairs are incoming donation status and expected value of accepting donations.
     */
    @ParameterizedTest
    @CsvSource({
      "Accepting Donations,true",
      "Accepting Requested Donations Only,true",
      "Not Accepting Donations,false",
      "Closed,false"
    })
    void donationStatus(String inputDonationStatus, String expectedIsAcceptingDonations) {
      var input = sampleData.toBuilder().donationStatus(inputDonationStatus).build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(200);
      String query =
          """
          select accepting_donations from site where airtable_id = :airtableId
          """;
      boolean result =
          TestConfiguration.jdbiTest.withHandle(
              handle ->
                  handle
                      .createQuery(query)
                      .bind("airtableId", input.getAirtableId())
                      .mapTo(Boolean.class)
                      .one());
      assertThat(result).isEqualTo(Boolean.valueOf(expectedIsAcceptingDonations));
    }

    /** Expected data pairs are incoming donation status and expected value of active. */
    @ParameterizedTest
    @CsvSource({
      "Accepting Donations,true",
      "Accepting Requested Donations Only,true",
      "Not Accepting Donations,true",
      "Closed,false"
    })
    void active(String inputDonationStatus, String expectedActive) {
      var input = sampleData.toBuilder().donationStatus(inputDonationStatus).build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(200);
      String query =
          """
          select accepting_donations from site where airtable_id = :airtableId
          """;
      boolean result =
          TestConfiguration.jdbiTest.withHandle(
              handle ->
                  handle
                      .createQuery(query)
                      .bind("airtableId", input.getAirtableId())
                      .mapTo(Boolean.class)
                      .one());
      assertThat(result).isEqualTo(Boolean.valueOf(expectedActive));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void publicVisibility(boolean isPubliclyVisible) {
      var input = sampleData.toBuilder().publicVisibility(isPubliclyVisible).build();

      var response = siteDataImportController.updateSiteData(input);

      assertThat(response.getStatusCode().value()).isEqualTo(200);
      String query =
          """
          select publicly_visible from site where airtable_id = :airtableId
          """;
      boolean result =
          TestConfiguration.jdbiTest.withHandle(
              handle ->
                  handle
                      .createQuery(query)
                      .bind("airtableId", input.getAirtableId())
                      .mapTo(Boolean.class)
                      .one());
      assertThat(result).isEqualTo(isPubliclyVisible);
    }
  }
}
