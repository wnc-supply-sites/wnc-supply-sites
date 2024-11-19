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
                              insert into site_item(site_id, item_id, item_status_id)
                              values (:siteId, :itemId, (select id from item_status where name = 'Oversupply'))
                          """)
                            .bind("siteId", siteId)
                            .bind("itemId", itemId)
                            .execute());
              } catch (Exception e) {
                if (e.getMessage().contains("duplicate key value violates")) {
                  try {
                    jdbi.withHandle(
                        handle ->
                            handle
                                .createUpdate(
                                    """
                                update site_item
                                set item_status_id = (select id from item_status where name = 'Oversupply'),
                                   last_updated = now()
                                where site_id = :siteId and item_id = :itemId
                            """)
                                .bind("siteId", siteId)
                                .bind("itemId", itemId)
                                .execute());
                  } catch (Exception ex) {
                    throw new RuntimeException(
                        String.format(
                            "Failed updating site supply item: %s, for site: %s",
                            item, supplyData.getSiteName()),
                        ex);
                  }
                } else {
                  throw new RuntimeException(
                      String.format(
                          "Error inserting site supply item: %s, for site: %s",
                          item, supplyData.getSiteName()),
                      e);
                }
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
