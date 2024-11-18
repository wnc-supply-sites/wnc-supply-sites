package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.manage.ManageSiteController.SiteSelection;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

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
}
