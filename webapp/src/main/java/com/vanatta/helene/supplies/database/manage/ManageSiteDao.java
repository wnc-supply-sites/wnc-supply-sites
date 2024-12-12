package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.manage.SelectSiteController.SiteSelection;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
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
    STREET_ADDRESS("address", "Street Address", true),
    CITY("city", "City", true),
    STATE("state", "State", true),
    COUNTY("county", "County", true),
    WEBSITE("website", "Website", false),
    FACEBOOK("facebook", "Facebook", false),
    SITE_HOURS("hours", "Site Hours", false),
    CONTACT_NAME("contact_name", "Contact Name", false),
    CONTACT_NUMBER("contact_number", "Contact Number", false),
    CONTACT_EMAIL("contact_email", "Contact Email", false),
    ADDITIONAL_CONTACTS("additional_contacts", "Additional Contacts", false),
    BAD_NUMBERS("bad_numbers", "Bad Numbers", false),
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

    if (field.isRequired() && (newValue == null || newValue.isEmpty())) {
      throw new RequiredFieldException(field.frontEndName);
    }

    @Nullable final String oldValue;
    if (field == SiteField.COUNTY || field == SiteField.STATE) {
      if (!newValue.contains(",")) {
        throw new IllegalStateException(
            "New county value must be encoded as 'COUNTY,STATE'; Illegal value: " + newValue);
      }
      String[] split = newValue.split(",");
      String county = split[0];
      String state = split[1];
      oldValue = updateCounty(jdbi, siteId, county, state);
    } else {
      oldValue = updateSiteColumn(jdbi, siteId, field, newValue);
    }
    addToAuditTrail(jdbi, siteId, field, oldValue, newValue);
  }

  /**
   * Returns old value and then updates. Old value is encoded as "COUNTY,STATE" (single string,
   * comma delimited)
   */
  //  @VisibleForTesting
  static String updateCounty(Jdbi jdbi, long siteId, String newCounty, String newState) {
    // first lookup the old value from database before we change it.
    String oldValueQuery =
        """
        select c.name || ',' || c.state
        from site s
        join county c on c.id = s.county_id
        where s.id = :siteId
        """;
    String oldValue =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(oldValueQuery) //
                    .bind("siteId", siteId)
                    .mapTo(String.class)
                    .one());

    // now actually update the county value
    String updateCounty =
        """
        update site
        set
          county_id = (select id from county where name = :name and state = :state)
        where id = :id
        """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(updateCounty)
                .bind("name", newCounty)
                .bind("state", newState)
                .bind("id", siteId)
                .execute());
    return oldValue;
  }

  /** Returns old value and then updates. */
  //  @VisibleForTesting
  static String updateSiteColumn(Jdbi jdbi, long siteId, SiteField column, String newValue) {

    String oldValueQuery =
        java.lang.String.format(
            "select s.%s from site s where s.id = :siteId", column.getColumnName());
    String oldValue =
        jdbi.withHandle(
            handle ->
                handle.createQuery(oldValueQuery).bind("siteId", siteId).mapTo(String.class).one());

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(
                    "update site set " + column.getColumnName() + " = :newValue where id = :siteId")
                .bind("newValue", newValue)
                .bind("siteId", siteId)
                .execute());
    return oldValue;
  }

  private static void addToAuditTrail(
      Jdbi jdbi, long siteId, SiteField field, String oldValue, String newValue) {
    String insert =
        """
    insert into site_audit_trail(site_id, field_name, old_value, new_value)
    values(
       :siteId, :fieldName, :oldValue, :newValue
    )
    """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(insert)
                .bind("siteId", siteId)
                .bind("fieldName", field.getColumnName())
                .bind("oldValue", oldValue)
                .bind("newValue", newValue)
                .execute());
  }

  /** Returns null if ID is not valid or DNE. */
  static String fetchSiteName(Jdbi jdbi, String siteId) {
    if (siteId == null || siteId.isBlank()) {
      return null;
    }

    try {
      long id = Long.parseLong(siteId);
      return ManageSiteDao.fetchSiteName(jdbi, id);
    } catch (NumberFormatException e) {
      return null;
    }
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
    boolean publiclyVisible;
    boolean acceptingDonations;
    boolean distributingSupplies;
    boolean onboarded;
    String inactiveReason;
    String siteType;

    public SiteType getSiteTypeEnum() {
      return Arrays.stream(SiteType.values())
          .filter(s -> s.getText().equals(siteType))
          .findAny()
          .orElseThrow(() -> new IllegalArgumentException("Unknown site type: " + siteType));
    }
  }

  public static SiteStatus fetchSiteStatus(Jdbi jdbi, long siteId) {
    String query =
        """
        select
          s.active,
          s.accepting_donations,
          s.publicly_visible,
          s.distributing_supplies,
          s.onboarded,
          s.inactive_reason,
          st.name siteType
        from site s
        join site_type st on st.id = s.site_type_id
        where s.id = :siteId
        """;

    SiteStatus siteStatus =
        jdbi.withHandle(
            handle ->
                handle.createQuery(query).bind("siteId", siteId).mapToBean(SiteStatus.class).one());
    if (siteStatus == null) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    } else {
      return siteStatus;
    }
  }

  public static void updateSiteOnboarded(Jdbi jdbi, long siteId, boolean newValue) {
    updateSiteFlag(jdbi, siteId, "onboarded", newValue);
  }

  public static void updateSiteAcceptingDonationsFlag(Jdbi jdbi, long siteId, boolean newValue) {
    updateSiteFlag(jdbi, siteId, "accepting_donations", newValue);
  }

  public static void updateSiteDistributingDonationsFlag(Jdbi jdbi, long siteId, boolean newValue) {
    updateSiteFlag(jdbi, siteId, "distributing_supplies", newValue);
  }

  public static void updateSiteActiveFlag(Jdbi jdbi, long siteId, boolean newValue) {
    updateSiteFlag(jdbi, siteId, "active", newValue);
  }

  public static void updateSitePubliclyVisible(Jdbi jdbi, long siteId, boolean newValue) {
    updateSiteFlag(jdbi, siteId, "publicly_visible", newValue);
  }

  private static void updateSiteFlag(Jdbi jdbi, long siteId, String column, boolean newValue) {
    String update =
        java.lang.String.format(
            "update site set %s = :newValue, last_updated = now() where id = :siteId", column);

    int updateCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(update)
                    .bind("newValue", newValue)
                    .bind("siteId", siteId)
                    .execute());

    if (updateCount == 0) {
      throw new IllegalArgumentException("Invalid site id: " + siteId);
    }
  }

  public static void updateInactiveReason(Jdbi jdbi, long siteId, String inactiveReason) {
    String update =
        """
        update site set inactive_reason = :inactiveReason where id = :siteId
        """;

    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("inactiveReason", inactiveReason)
                .bind("siteId", siteId)
                .execute());
  }

  /** Fetches all items, items requested/needed for a given site are listed as active. */
  public static List<SiteInventory> fetchSiteInventory(Jdbi jdbi, long siteId) {
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

  /**
   * Updates that the site inventory was last updated now, and because the site inventory was
   * updated, mark the site as onboarded.
   */
  public static void updateSiteInventoryLastUpdatedAndOnboardedFlag(Jdbi jdbi, long siteId) {
    String updateSiteLastUpdated =
        "update site set inventory_last_updated = now(), onboarded = true where id = :siteId";
    jdbi.withHandle(
        handle -> handle.createUpdate(updateSiteLastUpdated).bind("siteId", siteId).execute());
  }

  static void updateSiteType(Jdbi jdbi, long siteId, SiteType siteType) {
    String update =
        """
        update site set site_type_id = (select id from site_type where name = :siteTypeName)
           where id = :siteId;
        """;
    jdbi.withHandle(
        handle ->
            handle
                .createUpdate(update)
                .bind("siteId", siteId)
                .bind("siteTypeName", siteType.getText())
                .execute());
  }
}
