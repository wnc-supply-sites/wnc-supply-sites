package com.vanatta.helene.supplies.database.supplies.site.details;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class SiteDetailDao {

  @Nullable
  public static Long lookupSiteIdByAirtableId(Jdbi jdbi, long airtableId) {
    return lookupIdentifier(jdbi, "airtable_id", airtableId);
  }

  @Nullable
  public static Long lookupSiteIdByWssId(Jdbi jdbi, long wssId) {
    return lookupIdentifier(jdbi, "wss_id", wssId);
  }

  private static Long lookupIdentifier(Jdbi jdbi, String lookupColumn, long idValue) {
    String query = String.format("select id from site where %s = :id", lookupColumn);
    return jdbi.withHandle(
            handle -> handle.createQuery(query).bind("id", idValue).mapTo(Long.class).findOne())
        .orElse(null);
  }

  @Data
  @NoArgsConstructor
  public static class SiteDetailData {
    String siteName;
    String siteType;
    String contactName;
    String contactNumber;
    String contactEmail;
    String additionalContacts;
    String address;
    String city;
    String state;
    String county;
    String website;
    String facebook;
    boolean publiclyVisible;
    boolean active;
    boolean distributingSupplies;
    boolean acceptingDonations;
    String hours;
    long wssId;
    boolean onboarded;
    String badNumbers;
    String inactiveReason;
    String maxSupply;
    boolean hasForklift;
    boolean hasLoadingDock;
    boolean hasIndoorStorage;
    String receivingNotes;
  }

  @Nullable
  public static SiteDetailData lookupSiteById(Jdbi jdbi, long idToLookup) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                            select
                              s.name siteName,
                              st.name siteType,
                              s.contact_name,
                              s.contact_number,
                              s.contact_email,
                              s.additional_contacts,
                              s.address,
                              s.city,
                              c.state,
                              c.name county,
                              s.website,
                              s.facebook,
                              s.publicly_visible,
                              s.active,
                              s.distributing_supplies,
                              s.accepting_donations,
                              s.hours,
                              s.wss_id,
                              s.onboarded,
                              s.bad_numbers,
                              s.inactive_reason,
                              msl.name maxSupply,
                              s.has_forklift,
                              s.has_loading_dock,
                              s.has_indoor_storage,
                              s.receiving_notes
                            from site s
                            join county c on c.id = s.county_id
                            join site_type st on st.id = s.site_type_id
                            left join max_supply_load msl on msl.id = s.max_supply_load_id
                            where s.id = :siteId
                            """)
                .bind("siteId", idToLookup)
                .mapToBean(SiteDetailData.class)
                .findOne()
                .orElse(null));
  }
}
