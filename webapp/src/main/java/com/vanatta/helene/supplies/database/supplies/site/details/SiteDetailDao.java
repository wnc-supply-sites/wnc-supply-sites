package com.vanatta.helene.supplies.database.supplies.site.details;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class SiteDetailDao {

  @Data
  @NoArgsConstructor
  public static class SiteDetailData {
    String siteName;
    String siteType;
    String contactNumber;
    String address;
    String city;
    String state;
    String county;
    String website;
  }

  public static SiteDetailData lookupSiteById(Jdbi jdbi, long idToLookup) {
    SiteDetailData data =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        """
                            select
                              s.name siteName,
                              st.name siteType,
                              s.contact_number,
                              s.address,
                              s.city,
                              s.state,
                              s.website,
                              c.name county
                            from site s
                            join county c on c.id = s.county_id
                            join site_type st on st.id = s.site_type_id
                            where s.id = :siteId
                            """)
                    .bind("siteId", idToLookup)
                    .mapToBean(SiteDetailData.class)
                    .findOne()
                    .orElseThrow(
                        () -> new IllegalArgumentException("Invalid site id: " + idToLookup)));
    if (data == null) {
      throw new IllegalArgumentException("Site does not exist, id: " + idToLookup);
    }
    return data;
  }
}
