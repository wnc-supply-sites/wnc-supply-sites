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


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteInventory {
    long itemId;
    String itemName;
    String itemStatus;
    boolean activeRequest;
  }
}
