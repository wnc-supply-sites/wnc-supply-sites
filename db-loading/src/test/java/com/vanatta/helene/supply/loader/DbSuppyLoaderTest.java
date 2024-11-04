package com.vanatta.helene.supply.loader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DbSuppyLoaderTest {

  @BeforeAll
  static void setup() {
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site_need").execute());
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site_supply").execute());
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site").execute());
    DbSupplyLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from item").execute());

    DbSupplyLoader.jdbiTest.withHandle(
        handle ->
            handle
                .createUpdate(
                    "insert into site(name, address, city, county_id, state)"
                        + " values ('site-name', '123 address', 'some-city', "
                        + " (select id from county where name = 'Ashe'),"
                        + " 'NC')")
                .execute());

    DbSupplyLoader.jdbiTest.withHandle(
        handle -> handle.createUpdate("insert into item(name) values ('diaper')").execute());
    DbSupplyLoader.jdbiTest.withHandle(
        handle -> handle.createUpdate("insert into item(name) values ('n95 mask')").execute());
    DbSupplyLoader.jdbiTest.withHandle(
        handle -> handle.createUpdate("insert into item(name) values ('canned tomato')").execute());
  }

  @Test
  void load() {
    DbSupplyLoader.load(
        DbSupplyLoader.jdbiTest,
        List.of(SupplyData.builder().siteName("site-name").items("diaper, n95 mask").build()));

    int count =
        DbSupplyLoader.jdbiTest.withHandle(
            handle ->
                handle.createQuery("select count(*) from site_supply").mapTo(Integer.class).one());

    assertThat(count).isEqualTo(2);
  }
}
