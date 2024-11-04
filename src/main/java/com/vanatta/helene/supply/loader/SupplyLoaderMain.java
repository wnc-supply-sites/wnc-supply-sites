package com.vanatta.helene.supply.loader;

import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class SupplyLoaderMain {

  public static void main(String[] args) {
    loadData(DbSupplyLoader.jdbi, "/supplies-helene-list.csv");
  }

  static void loadData(Jdbi jdbi, String fileName) {
    jdbi.withHandle(handle -> handle.createUpdate("delete from site_supply").execute());

    List<SupplyData> csvData = SupplyCsvReader.read(fileName);
    DbSupplyLoader.load(jdbi, csvData);
  }
}
