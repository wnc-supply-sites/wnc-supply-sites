package com.vanatta.helene.supplies.database.data.export;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class DataExportDao {

  @Data
  @NoArgsConstructor
  public static class SiteExportData {
    String siteName;
    String siteType;
    String contactNumber;
    String address;
    String city;
    String state;
    String county;
    String website;
    String donationStatus;
    boolean active;
  }

  public static List<SiteExportData> fetchAllSites(Jdbi jdbi) {
    return jdbi.withHandle(
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
                              c.name county,
                              case when s.accepting_donations then 'Accepting Donations' else 'Not Accepting Donations' end donationStatus,
                              s.active
                            from site s
                            join county c on c.id = s.county_id
                            join site_type st on st.id = s.site_type_id
                            """)
                .mapToBean(SiteExportData.class)
                .list());
  }
}
