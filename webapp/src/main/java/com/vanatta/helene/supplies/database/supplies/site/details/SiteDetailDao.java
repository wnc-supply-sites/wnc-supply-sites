package com.vanatta.helene.supplies.database.supplies.site.details;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class SiteDetailDao {

  @Data
  @NoArgsConstructor
  public static class SiteDetailData {
    String siteName;
    String siteType;
    String contactName;
    String contactNumber;
    String contactEmail;
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
                              s.address,
                              s.city,
                              c.state,
                              c.name county,
                              s.website,
                              s.facebook,
                              s.public_visibility,
                              s.active,
                              s.distributing_supplies,
                              s.accepting_donations,
                              s.hours
                            from site s
                            join county c on c.id = s.county_id
                            join site_type st on st.id = s.site_type_id
                            where s.id = :siteId
                            """)
                .bind("siteId", idToLookup)
                .mapToBean(SiteDetailData.class)
                .findOne()
                .orElseThrow(null));
  }
}
