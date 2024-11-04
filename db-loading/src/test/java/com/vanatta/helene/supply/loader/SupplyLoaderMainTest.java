package com.vanatta.helene.supply.loader;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class SupplyLoaderMainTest {

  @BeforeAll
  static void setup() {
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site_need").execute());
    DbSupplyLoader.jdbiTest.withHandle(
        handle -> handle.createUpdate("delete from site_supply").execute());
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site").execute());
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from item").execute());

    // create sites in DB
    List.of(
            "Jimmy & Jeans",
            "Tdd's Table Little Free Pantry at Blackburn's Chapel",
            "Plumtree Church",
            "Covenant Community Church")
        .forEach(
            siteName ->
                DbSupplyLoader.jdbiTest.withHandle(
                    handle ->
                        handle
                            .createUpdate(
                                "insert into site(name, address, city, county_id, state)"
                                    + " values (:siteName, '123 address', 'some-city', "
                                    + " (select id from county where name = 'Ashe'),"
                                    + " 'NC')")
                            .bind("siteName", siteName)
                            .execute()));

    // create items in DB
    List.of(
            "adult diaper",
            "diapers",
            "wipes",
            "clothing",
            "formula",
            "Toothpaste",
            "toothbrushes",
            "feminine hygiene",
            "shampoo",
            "lotion",
            "soap")
        .forEach(
            itemName ->
                DbSupplyLoader.jdbiTest.withHandle(
                    handle ->
                        handle
                            .createUpdate("insert into item(name) values(:itemName)")
                            .bind("itemName", itemName)
                            .execute()));
  }

  @Test
  void sampleLoad() {
    SupplyLoaderMain.loadData(DbSupplyLoader.jdbiTest, "/test-supplies-helene-list.csv");

    int rowCount =
        DbSupplyLoader.jdbiTest.withHandle(
            handle ->
                handle.createQuery("select count(*) from site_supply").mapTo(Integer.class).one());
    Assertions.assertThat(rowCount).isNotZero();
  }
}
