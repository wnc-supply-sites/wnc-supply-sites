package com.vanatta.helene.supply.loader;

import org.jdbi.v3.core.Jdbi;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DbSupplyLoader {
  static final Jdbi jdbi =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene", "wnc_helene", "wnc_helene");

  static final Jdbi jdbiTest =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene_test", "wnc_helene", "wnc_helene");

  public static void load(Jdbi jdbi, List<SupplyData> supplyData) {
    supplyData.forEach(supply -> loadSupplyData(jdbi, supply));
  }

  private static void loadSupplyData(Jdbi jdbi, SupplyData supplyData) {
    Long siteId = lookupSiteId(jdbi, supplyData.getSiteName());

    Arrays.stream(supplyData.getItems().split(","))
        .filter(Objects::nonNull)
        .map(String::trim)
        .map(String::toLowerCase)
        .sorted()
        .distinct()
        .forEach(
            item -> {
              Long itemId = lookupItemId(jdbi, item);

              try {
                jdbi.withHandle(
                    handle ->
                        handle
                            .createUpdate(
                                """
                              insert into site_supply(site_id, item_id)
                              values (:siteId, :itemId)
                          """)
                            .bind("siteId", siteId)
                            .bind("itemId", itemId)
                            .execute());
              } catch (Exception e) {
                throw new RuntimeException(
                    String.format(
                        "Error inserting site supply item: %s, for site: %s",
                        item, supplyData.getSiteName()),
                    e);
              }
            });
  }

  private static Long lookupSiteId(Jdbi jdbi, String siteName) {
    try {
      return jdbi.withHandle(
          handle ->
              handle
                  .createQuery("select id from site where lower(name) = :name")
                  .bind("name", siteName.toLowerCase().trim())
                  .mapTo(Long.class)
                  .one());
    } catch (IllegalStateException e) {
      throw new IllegalArgumentException("Invalid site name (check spelling): " + siteName);
    }
  }

  private static Long lookupItemId(Jdbi jdbi, String item) {
    try {
      return jdbi.withHandle(
          handle ->
              handle
                  .createQuery("select id from item where lower(name) = :name")
                  .bind("name", item.toLowerCase())
                  .mapTo(Long.class)
                  .one());
    } catch (IllegalStateException e) {
      throw new IllegalArgumentException(
          "Invalid item name (check spelling & exists in item table): " + item);
    }
  }
}
