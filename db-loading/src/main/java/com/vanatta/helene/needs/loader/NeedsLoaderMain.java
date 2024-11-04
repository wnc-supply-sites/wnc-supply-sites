package com.vanatta.helene.needs.loader;

import org.jdbi.v3.core.Jdbi;

public class NeedsLoaderMain {

  public static void main(String[] args) {
    loadData(DbNeedsLoader.jdbi, "/helene_need_list.csv");
  }

  static void loadData(Jdbi jdbi, String fileName) {
    jdbi.withHandle(handle -> handle.createUpdate("delete from site_need").execute());
    jdbi.withHandle(handle -> handle.createUpdate("delete from site_supply").execute());
    jdbi.withHandle(handle -> handle.createUpdate("delete from site").execute());

    var csvData = NeedsCsvLoader.readFile(fileName);
    DbNeedsLoader.populate(jdbi, csvData);
  }
}
