package com.vanatta.helene.supplies.database.supplies;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SuppliesControllerTest {

  static final Jdbi jdbiTest = TestConfiguration.jdbiTest;

  private final SuppliesController suppliesController = new SuppliesController(jdbiTest);

  @BeforeAll
  static void clearDatabase() {
    TestConfiguration.setupDatabase();
  }

  @Test
  void emptyRequestReturnsData() {
    var result = suppliesController.getSuppliesData(SiteSupplyRequest.builder().build());

    // 2 sites, should be two results
    assertThat(result.getResultCount()).isEqualTo(2);
  }

  @Test
  void requestBySite() {
    var result =
        suppliesController.getSuppliesData(SiteSupplyRequest.builder().sites(List.of()).build());
    assertThat(result.getResultCount()).isEqualTo(2);

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().sites(List.of("site1")).build());

    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getSite()).isEqualTo("site1");

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().sites(List.of("site1", "site2")).build());

    assertThat(result.getResultCount()).isEqualTo(2);
  }

  @Test
  void requestByItem() {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().items(List.of("water")).build());

    assertThat(result.getResultCount()).isEqualTo(2);

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().items(List.of("water", "new clothes")).build());
    assertThat(result.getResultCount()).isEqualTo(2);

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().items(List.of("random stuff")).build());
    assertThat(result.getResultCount()).isEqualTo(0);

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().items(List.of("random stuff", "new clothes")).build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getItems().getFirst().getName())
        .isEqualTo("new clothes");
    assertThat(result.getResults().getFirst().getItems().getFirst().getStatus())
        .isEqualTo("Urgent Need");
  }

  @Test
  void requestByCounty() {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Haywood")).build());
    assertThat(result.getResultCount()).isEqualTo(0);

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Buncombe")).build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getCounty()).isEqualTo("Buncombe");

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Watauga")).build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getCounty()).isEqualTo("Watauga");

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Ashe", "Buncombe")).build());
    assertThat(result.getResultCount()).isEqualTo(1);

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Watauga", "Buncombe")).build());
    assertThat(result.getResultCount()).isEqualTo(2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"Urgent Need", "Oversupply", "Requested"})
  void requestByItemStatus(String itemStatus) {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().itemStatus(List.of(itemStatus)).build());
    result.getResults().stream()
        .map(SiteSupplyResponse.SiteSupplyData::getItems)
        .flatMap(List::stream)
        .forEach(item -> assertThat(item.getStatus()).isEqualTo(itemStatus));
  }

  @Test
  void multipleItemStatus() {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().itemStatus(List.of("Oversupply", "Urgent Need")).build());
    result.getResults().stream()
        .map(SiteSupplyResponse.SiteSupplyData::getItems)
        .flatMap(List::stream)
        .forEach(item -> assertThat(item.getStatus()).isIn("Oversupply", "Urgent Need"));
  }

  @Test
  void mixedRequest() {
    // no sites with the requested county, or requested item
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .counties(List.of("Haywood"))
                .items(List.of("Random stuff"))
                .build());
    assertThat(result.getResultCount()).isEqualTo(0);

    // there exists a site with 'Watauga' county, but no items
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .counties(List.of("Watauga"))
                .items(List.of("Random stuff"))
                .build());
    assertThat(result.getResultCount()).isEqualTo(0);

    // there exists a site with the target county, but no item
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .sites(List.of("site1"))
                .counties(List.of("Watauga"))
                .items(List.of("Random stuff"))
                .build());
    assertThat(result.getResultCount()).isEqualTo(0);

    // site name and county match
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .sites(List.of("site1"))
                .counties(List.of("Watauga"))
                .build());
    assertThat(result.getResultCount()).isEqualTo(1);

    // both sites have water, but only one is in Watauga county
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .items(List.of("water"))
                .counties(List.of("Watauga"))
                .build());
    assertThat(result.getResultCount()).isEqualTo(1);
  }

  /** Validate that we aggregate the item list together by site. */
  @Test
  void multipleItemsAreAggregated() {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().sites(List.of("site1")).build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getItems()).hasSize(3);
  }

  /**
   * We have one site that is accepting donations and active, we should only get one site back when
   * querying for sites that are accepting donations.
   */
  @Test
  void queryForAcceptingDonationsOnly() {
    // show exactly the sites accepting donations
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .acceptingDonations(true)
                .notAcceptingDonations(false)
                .build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().isAcceptingDonations()).isTrue();

    // show exactly the sites not accepting donations
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .acceptingDonations(false)
                .notAcceptingDonations(true)
                .build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().isAcceptingDonations()).isFalse();

    // show all sites (false to both would always return no results, instead we just ignore the flag
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .acceptingDonations(false)
                .notAcceptingDonations(false)
                .build());
    assertThat(result.getResultCount()).isEqualTo(2);

    // show all sites
    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .acceptingDonations(true)
                .notAcceptingDonations(true)
                .build());
    assertThat(result.getResultCount()).isEqualTo(2);
  }
}
