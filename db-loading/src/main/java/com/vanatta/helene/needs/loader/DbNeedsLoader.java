package com.vanatta.helene.needs.loader;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.util.*;

/**
 * Takes as input data parsed from CSV, loads that data into database. Assumes that we have set up a
 * schema and inserted into the item_category table.
 */
public class DbNeedsLoader {

  static final Jdbi jdbi =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene", "wnc_helene", "wnc_helene");

  static final Jdbi jdbiTest =
      Jdbi.create("jdbc:postgresql://localhost:5432/wnc_helene_test", "wnc_helene", "wnc_helene");

  static void populate(Jdbi jdbi, List<CsvDistroData> sites) {
    loadOrganizations(jdbi, sites);
    loadItems(jdbi, sites);
  }

  static void loadOrganizations(Jdbi jdbi, List<CsvDistroData> sites) {
    String sql =
        """
        insert into site(name, address, city, county_id, state, accepting_donations) values (
           :name, :address, :city,
           (select id from county where name = :countyName),
           :state,
           :acceptingDonations)
        """;

    sites.forEach(
        site -> {
          boolean acceptingDonations =
              site.getDonationCenterStatus()
                  .toUpperCase()
                  .startsWith("Accepting Donations".toUpperCase());

          try {
            jdbi.withHandle(
                handle ->
                    handle
                        .createUpdate(sql)
                        .bind("name", site.getOrganizationName())
                        .bind("address", site.getStreetAddress())
                        .bind("city", site.getCity())
                        .bind("countyName", site.getCounty())
                        .bind("state", site.getState())
                        .bind("acceptingDonations", acceptingDonations)
                        .execute());
          } catch (Exception e) {
            throw new RuntimeException("Error create site: " + site, e);
          }
        });
  }

  public static void loadItems(Jdbi jdbi, List<CsvDistroData> data) {
    data.forEach(datum -> loadItems(jdbi, datum));
  }

  private static void loadItems(Jdbi jdbi, CsvDistroData siteData) {
    List<String> items = new ArrayList<>();
    //    loadItem(jdbi, "Other Items Not Listed", data, CsvDistroData::getOtherItemsNotListed);
    items.add(siteData.getWinterGear());
    items.add(siteData.getWinterGear());
    items.add(siteData.getAnimalSupplies());
    items.add(siteData.getAppliances());
    items.add(siteData.getBabyItems());
    items.add(siteData.getCleaningSupplies());
    items.add(siteData.getCleanup());
    items.add(siteData.getClothing());
    items.add(siteData.getEmergencyItems());
    items.add(siteData.getEquipment());
    items.add(siteData.getFirstAid());
    items.add(siteData.getFood());
    items.add(siteData.getFuelOil());
    items.add(siteData.getHydration());
    items.add(siteData.getKidsToys());
    items.add(siteData.getLinen());
    items.add(siteData.getMedsAdult());
    items.add(siteData.getMedsChild());
    items.add(siteData.getOtcMisc());
    items.add(siteData.getPaperProducts());
    items.add(siteData.getToiletries());

    List<String> allItems =
        items.stream()
            .filter(Objects::nonNull)
            .flatMap(s -> Arrays.stream(s.split("\n")))
            .map(value -> value.isBlank() ? null : value.trim())
            .filter(Objects::nonNull)
            .sorted()
            .distinct()
            .toList();

    if (allItems.isEmpty()) {
      return;
    }

    // first load up the items into the item table
    String insertItemSql =
        """
          insert into item (name) values (:itemName)
         """;

    allItems.forEach(
        itemName -> {
          try {
            jdbi.withHandle(
                handle -> handle.createUpdate(insertItemSql).bind("itemName", itemName).execute());
          } catch (UnableToExecuteStatementException e) {
            // ignore errors inserting duplicate values (let it fail and move on)
            if (!e.getMessage().contains("duplicate key value")) {
              throw new RuntimeException("Error inserting item: " + itemName, e);
            }
          } catch (Exception e) {
            throw new RuntimeException("Error inserting item: " + itemName, e);
          }
        });

    // next, load up the item needs for the specific location
    String insertSiteNeedsSql =
        """
           insert into site_need (site_id, item_id)
             values(
                (select id from site where name = :siteName),
                (select id from item where name = :itemName)
             )
        """;
    allItems.forEach(
        item -> {
          try {
            jdbi.withHandle(
                handle ->
                    handle
                        .createUpdate(insertSiteNeedsSql)
                        .bind("siteName", siteData.getOrganizationName())
                        .bind("itemName", item)
                        .execute());
          } catch (Exception e) {
            throw new RuntimeException(
                String.format(
                    "Failed to insert site need item: '%s', site name: '%s'",
                    item, siteData.getOrganizationName()),
                e);
          }
        });
  }
}
