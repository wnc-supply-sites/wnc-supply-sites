package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.manage.ManageSiteController.SiteSelection;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class ManageSiteDao {

  static List<SiteSelection> fetchSiteList(Jdbi jdbi) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery("select id, name from site order by name")
                .mapToBean(SiteSelection.class)
                .list());
  }

  public static void updateSiteContact(Jdbi jdbi, long siteId, String newContact) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate("update site set contact_number = :contactNumber where id = :siteId")
                .bind("contactNumber", newContact)
                .bind("siteId", siteId)
                .execute());
  }

  @Nullable
  public static String fetchSiteContact(Jdbi jdbi, long siteId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery("select contact_number from site where id = :siteId")
                .bind("siteId", siteId)
                .mapTo(String.class)
                .one());
  }

  @Nullable
  public static String fetchSiteName(Jdbi jdbi, long siteId) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery("select name from site where id = :siteId")
                .bind("siteId", siteId)
                .mapTo(String.class)
                .one());
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteStatus {
    boolean active;
    boolean acceptingDonations;
  }

  public static SiteStatus fetchSiteStatus(Jdbi jdbi, long siteId) {
    SiteStatus siteStatus =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("select active, accepting_donations from site where id = :siteId")
                    .bind("siteId", siteId)
                    .mapToBean(SiteStatus.class)
                    .one());
    if (siteStatus == null) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    } else {
      return siteStatus;
    }
  }

  public static void updateSiteAcceptingDonationsFlag(Jdbi jdbi, long siteId, boolean newValue) {
    int updateCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(
                        "update site set accepting_donations = :newValue where id = :siteId")
                    .bind("newValue", newValue)
                    .bind("siteId", siteId)
                    .execute());

    if (updateCount == 0) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    }
  }

  public static void updateSiteActiveFlag(Jdbi jdbi, long siteId, boolean newValue) {
    int updateCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate("update site set active = :newValue where id = :siteId")
                    .bind("newValue", newValue)
                    .bind("siteId", siteId)
                    .execute());

    if (updateCount == 0) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    }
  }

  /** Fetches all items, items requested/needed for a given site are listed as active. */
  static List<SiteInventory> fetchSiteInventory(Jdbi jdbi, long siteId) {
    String query =
        """
        with inventory as (
          select
              i.id item_id,
              s.id site_id,
              stat.name status_name
         from site s
         join site_item si on si.site_id = s.id
         join item i on i.id = si.item_id
         join item_status stat on stat.id = si.item_status_id
         where s.id = :siteId
        )
        select
            i.id item_id,
            i.name item_name,
            case when inv.site_id is null then false else true end active,
            inv.status_name item_status
        from item i
        left join inventory inv on inv.item_id = i.id
    """;

    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query)
                .bind("siteId", siteId) //
                .mapToBean(SiteInventory.class)
                .list());
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteInventory {
    long itemId;
    String itemName;
    String itemStatus;
    boolean active;
  }

  static void updateSiteItemInactive(Jdbi jdbi, long siteId, String itemName) {
    String delete =
        """
            delete from site_item
            where site_id = :siteId
              and item_id = (select id from item where name = :itemName)
            """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(delete)
                .bind("siteId", siteId)
                .bind("itemName", itemName)
                .execute());
  }

  static void updateSiteItemActive(Jdbi jdbi, long siteId, String itemName, String itemStatus) {
    String insert =
        """
          insert into site_item(site_id, item_id, item_status_id) values
             (
                :siteId,
                (select id from item where name = :itemName),
                (select id from item_status where name = :itemStatus)
             )
          """;
    try {
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insert)
                  .bind("siteId", siteId)
                  .bind("itemName", itemName)
                  .bind("itemStatus", itemStatus)
                  .execute());
    } catch (Exception e) {
      if (e.getMessage().contains("already exists.")
          || (e.getCause() != null && e.getCause().getMessage().contains("duplicate key value"))) {
        log.warn(
            "Duplicate key insert attempted, siteId: {}, itemName: {}, itemStatus: {}",
            siteId,
            itemName,
            itemStatus);
      } else {
        throw e;
      }
    }
  }

  static void updateItemStatus(Jdbi jdbi, long siteId, String itemName, String itemStatus) {
    String update =
        """
      update site_item 
      set item_status_id = (select id from item_status where name = :itemStatus)
      where site_id = :siteId
         and item_id = (select id from item where name = :itemName)
      """;
    int updateCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(update)
                    .bind("siteId", siteId)
                    .bind("itemName", itemName)
                    .bind("itemStatus", itemStatus)
                    .execute());

    if (updateCount != 1) {
      throw new IllegalArgumentException(
          String.format("Invalid site id: %s, item name: %s", siteId, itemName));
    }
  }
}
