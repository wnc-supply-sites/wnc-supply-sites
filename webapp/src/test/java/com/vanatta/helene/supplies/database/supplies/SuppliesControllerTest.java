package com.vanatta.helene.supplies.database.supplies;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SuppliesControllerTest {

  static final Jdbi jdbiTest =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene_test", "wnc_helene", "wnc_helene")
          .installPlugin(new SqlObjectPlugin());

  private final SuppliesController suppliesController = new SuppliesController(jdbiTest);

  @BeforeAll
  static void clearDatabase() {
    List.of(
            "delete from site_item",
            "delete from item",
            "delete from site",
            """
                insert into site(name, address, city, county_id, state) values(
                   'site1', 'address1', 'city1', (select id from county where name = 'Watauga'), 'NC'
                );
                """,
            """
                insert into site(name, address, city, county_id, state) values(
                   'site2', 'address2', 'city2', (select id from county where name = 'Buncombe'), 'NC'
                );
                """,
            "insert into item(name) values('water')",
            "insert into item(name) values('gloves')",
            "insert into item(name) values('used clothes')",
            "insert into item(name) values('new clothes')",
            "insert into item(name) values('random stuff')",
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site1'),
                (select id from item where name = 'water'),
                (select id from item_status where name = 'Requested')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site1'),
                (select id from item where name = 'new clothes'),
                (select id from item_status where name = 'Urgent Need')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site2'),
                (select id from item where name = 'used clothes'),
                (select id from item_status where name = 'Oversupply')
               )
            """,
            """
               insert into site_item(site_id, item_id, item_status_id) values(
                (select id from site where name = 'site2'),
                (select id from item where name = 'water'),
                (select id from item_status where name = 'Oversupply')
               )
            """)
        .forEach(sql -> jdbiTest.withHandle(handle -> handle.createUpdate(sql).execute()));
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

  @Test
  void multipleItemsAreAggregated() {
    var result =
        suppliesController.getSuppliesData(
            SiteSupplyRequest.builder().sites(List.of("site1")).build());
    assertThat(result.getResultCount()).isEqualTo(1);
    assertThat(result.getResults().getFirst().getItems()).hasSize(2);
  }
}
