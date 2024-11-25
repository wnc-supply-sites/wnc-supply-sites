package com.vanatta.helene.supplies.database.supplies;

import static org.assertj.core.api.Assertions.assertThat;

import com.vanatta.helene.supplies.database.TestConfiguration;
import com.vanatta.helene.supplies.database.data.ItemStatus;
import com.vanatta.helene.supplies.database.data.SiteType;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
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

    // All active sites should be returned
    assertThat(result.getResultCount()).isEqualTo(4);
  }

  @Test
  void requestSiteWithNoItems() {
    // because we list all item statuses, we will effectively ignore that list and return sites
    // that have no items.
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .itemStatus(ItemStatus.allItemStatus())
                .sites(List.of("site5"))
                .build());

    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getItems()).isEmpty();
  }

  @Test
  void requestBySite() {
    var result =
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
    assertThat(result.getResults().getFirst().getItems().getFirst().getDisplayClass())
        .isEqualTo(ItemStatus.URGENTLY_NEEDED.getCssClass());
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
    result.getResults().forEach(r -> assertThat(r.getCounty()).isEqualTo("Buncombe"));
    assertThat(
            result.getResults().stream().map(SiteSupplyResponse.SiteSupplyData::getSite).toList())
        .contains("site2", "site4");

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Watauga")).build());
    assertThat(
            result.getResults().stream().map(SiteSupplyResponse.SiteSupplyData::getSite).toList())
        .contains("site1");
    result.getResults().forEach(r -> assertThat(r.getCounty()).isEqualTo("Watauga"));

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Ashe", "Watauga", "Buncombe")).build());
    assertThat(
            result.getResults().stream().map(SiteSupplyResponse.SiteSupplyData::getSite).toList())
        .contains("site1", "site2", "site4");

    result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().counties(List.of("Ashe", "Buncombe")).build());
    assertThat(
            result.getResults().stream().map(SiteSupplyResponse.SiteSupplyData::getSite).toList())
        .contains("site2", "site4");
  }

  @Test
  void requestByItemStatus() {
    for(ItemStatus status : ItemStatus.values()) {
      var result =
          suppliesController.getSuppliesData(
              SiteSupplyRequest.builder().itemStatus(List.of(status.getText())).build());
      result.getResults().stream()
          .map(SiteSupplyResponse.SiteSupplyData::getItems)
          .flatMap(List::stream)
          .forEach(item -> assertThat(item.getDisplayClass()).isEqualTo(status.getCssClass()));
    }
  }

  @Test
  void multipleItemStatus() {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder()
                .itemStatus(
                    List.of(ItemStatus.OVERSUPPLY.getText(), ItemStatus.URGENTLY_NEEDED.getText()))
                .build());
    result.getResults().stream()
        .map(SiteSupplyResponse.SiteSupplyData::getItems)
        .flatMap(List::stream)
        .forEach(
            item ->
                assertThat(item.getDisplayClass())
                    .isIn(ItemStatus.OVERSUPPLY.getCssClass(), ItemStatus.URGENTLY_NEEDED.getCssClass()));
  }

  @Test
  void mixedRequestWithDifferentFilters() {
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
    // must contain sites that are active & accepting donations
    // (we exclude site5 because its name can change
    assertThat(result.getResults().stream().map(SiteSupplyResponse.SiteSupplyData::getSite))
        .contains("site1", "site4");
    // site2 is definitely excluded because active and not accepting donations
    // site3 is excluded because it is not active
    assertThat(result.getResults().stream().map(SiteSupplyResponse.SiteSupplyData::getSite))
        .doesNotContain("site2", "site3");

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
    int resultCount =
        suppliesController
            .getSuppliesData(
                SiteSupplyRequest.builder()
                    .acceptingDonations(false)
                    .notAcceptingDonations(false)
                    .build())
            .getResultCount();

    // show all sites
    int allSiteResultCount =
        suppliesController
            .getSuppliesData(
                SiteSupplyRequest.builder()
                    .acceptingDonations(true)
                    .notAcceptingDonations(true)
                    .build())
            .getResultCount();

    assertThat(resultCount).isEqualTo(allSiteResultCount);
  }

  @Nested
  class SiteTypeTest {
    /** Specify a specific site type, all results should come back with that site type */
    @ParameterizedTest
    @ValueSource(strings = {"Supply Hub", "Distribution Center"})
    void siteType(String siteType) {
      var result =
          suppliesController.getSuppliesData(
              SiteSupplyRequest.builder().siteType(List.of(siteType)).build());

      assertThat(result.getResultCount()).isGreaterThan(0);
      result.getResults().forEach(r -> assertThat(r.getSiteType()).isEqualTo(siteType));
    }

    /** Querying for neither site type, or all site types should return the same results */
    @Test
    void noSiteTypeOrAllSiteTypes() {
      var noSiteTypes =
          suppliesController.getSuppliesData(
              SiteSupplyRequest.builder().siteType(List.of()).build());
      var allSiteTypes =
          suppliesController.getSuppliesData(
              SiteSupplyRequest.builder().siteType(SiteType.allSiteTypes()).build());
      assertThat(noSiteTypes.getResultCount()).isGreaterThan(0);
      assertThat(noSiteTypes.getResultCount()).isEqualTo(allSiteTypes.getResultCount());
    }
  }
}
