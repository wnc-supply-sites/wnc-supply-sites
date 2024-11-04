package com.vanatta.helene.needs.loader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NeedsLoaderMainTest {

  @BeforeAll
  static void clearDatabase() {
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site_need").execute());
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site_supply").execute());
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from site").execute());
    DbNeedsLoader.jdbiTest.withHandle(handle -> handle.createUpdate("delete from item").execute());
  }

  @Test
  void integTest() {
    NeedsLoaderMain.loadData(DbNeedsLoader.jdbiTest, "/test_helene_list.csv");
  }
}
