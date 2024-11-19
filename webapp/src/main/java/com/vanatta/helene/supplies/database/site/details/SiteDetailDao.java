package com.vanatta.helene.supplies.database.site.details;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class SiteDetailDao {

  @Data
  @NoArgsConstructor
  public static class SiteDetailData {
    String siteName;
    String contactNumber;
    String address;
    String city;
    String state;
  }

  public static SiteDetailData lookupSiteById(Jdbi jdbi, long idToLookup) {
    SiteDetailData data =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        """
                            select s.name siteName, s.contact_number, s.address, s.city, s.state
                            from site s
                            where s.id = :siteId
                            """)
                    .bind("siteId", idToLookup)
                    .mapToBean(SiteDetailData.class)
                    .one());
    if (data == null) {
      throw new IllegalArgumentException("Site does not exist, id: " + idToLookup);
    }
    return data;
  }
}
