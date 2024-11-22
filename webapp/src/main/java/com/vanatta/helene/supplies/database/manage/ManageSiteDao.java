package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.manage.ManageSiteController.SiteSelection;
import com.vanatta.helene.supplies.database.supplies.SiteSupplyRequest;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class ManageSiteDao {

  static List<SiteSelection> fetchSiteList(Jdbi jdbi) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery("select id, name from site order by lower(name)")
                .mapToBean(SiteSelection.class)
                .list());
  }

  @AllArgsConstructor
  @Getter
  public enum SiteField {
    SITE_NAME("name", "Site Name", true),
    CONTACT_NUMBER("contact_number", "Contact Number", false),
    WEBSITE("website", "Website", false),
    STREET_ADDRESS("address", "Street Address", true),
    CITY("city", "City", true),
    COUNTY("county", "County", true),
    ;

    private final String columnName;
    private final String frontEndName;
    private final boolean required;

    static SiteField lookupField(String name) {
      return Arrays.stream(SiteField.values())
          .filter(f -> f.frontEndName.equals(name))
          .findAny()
          .orElseThrow(() -> new IllegalArgumentException("Invalid field name: " + name));
    }
  }

  static class RequiredFieldException extends IllegalArgumentException {
    RequiredFieldException(String fieldName) {
      super("Required field " + fieldName + " cannot be deleted");
    }
  }

  public static void updateSiteField(Jdbi jdbi, long siteId, SiteField field, String newValue) {
    log.info("Updating site: {}, field: {}, value: {}", siteId, field, newValue);

    if(field.isRequired() && (newValue == null || newValue.isEmpty()) ){
      throw new RequiredFieldException(field.frontEndName);
    }

    if (field == SiteField.COUNTY) {
      updateCounty(jdbi, siteId, Optional.ofNullable(newValue).orElse(""));
    } else {
      updateSiteColumn(jdbi, siteId, field.getColumnName(), newValue);
    }
  }

  /** Updating a county potentially requires us to create the county first, before updating it. */
  private static void updateCounty(Jdbi jdbi, long siteId, String newValue) {
    String selectCounty =
        """
          select id from county where name = :name
        """;
    String insertCounty =
        """
          insert into county(name) values(:name)
        """;
    String updateCounty =
        """
          update site set county_id = :countyId where id = :id
        """;
    Long countyId =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(selectCounty)
                    .bind("name", newValue)
                    .mapTo(Long.class)
                    .findOne()
                    .orElse(null));
    if (countyId == null) {
      jdbi.withHandle(handle -> handle.createUpdate(insertCounty).bind("name", newValue).execute());

      countyId =
          jdbi.withHandle(
              handle ->
                  handle.createQuery(selectCounty).bind("name", newValue).mapTo(Long.class).one());
    }
    final long countyIdToUse = countyId;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(updateCounty)
                .bind("countyId", countyIdToUse)
                .bind("id", siteId)
                .execute());
  }

  private static void updateSiteColumn(Jdbi jdbi, long siteId, String column, String newValue) {
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate("update site set " + column + " = :newValue where id = :siteId")
                .bind("newValue", newValue)
                .bind("siteId", siteId)
                .execute());
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
                        "update site set accepting_donations = :newValue, last_updated = now() where id = :siteId")
                    .bind("newValue", newValue)
                    .bind("siteId", siteId)
                    .execute());

    if (updateCount == 0) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    }
    updateSiteLastUpdatedToNow(jdbi, siteId);
  }

  public static void updateSiteActiveFlag(Jdbi jdbi, long siteId, boolean newValue) {
    int updateCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(
                        "update site set active = :newValue, last_updated = now() where id = :siteId")
                    .bind("newValue", newValue)
                    .bind("siteId", siteId)
                    .execute());

    if (updateCount == 0) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    }
    updateSiteLastUpdatedToNow(jdbi, siteId);
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
    updateSiteLastUpdatedToNow(jdbi, siteId);
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
    updateSiteLastUpdatedToNow(jdbi, siteId);
  }

  static void updateItemStatus(Jdbi jdbi, long siteId, String itemName, String itemStatus) {
    if(!SiteSupplyRequest.ItemStatus.allItemStatus().contains(itemStatus)) {
      throw new IllegalArgumentException("Invalid item status: " + itemStatus);
    }

    String update =
        """
      update site_item
      set item_status_id = (select id from item_status where name = :itemStatus),
         last_updated = now()
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
      throw new IllegalArgumentException(String.format("Invalid item name: %s", itemName));
    }
    updateSiteLastUpdatedToNow(jdbi, siteId);
  }

  private static void updateSiteLastUpdatedToNow(Jdbi jdbi, long siteId) {
    String updateSiteLastUpdated = "update site set last_updated = now() where id = :siteId";
    jdbi.withHandle(
        handle -> handle.createUpdate(updateSiteLastUpdated).bind("siteId", siteId).execute());
  }

  public static boolean addNewItem(Jdbi jdbi, String itemName) {
    String insert = "insert into item(name) values(:itemName)";

    try {
      jdbi.withHandle(handle -> handle.createUpdate(insert).bind("itemName", itemName).execute());
    } catch (Exception e) {
      if (e.getMessage().contains("duplicate key")) {
        return false;
      } else {
        throw e;
      }
    }
    return true;
  }
}
