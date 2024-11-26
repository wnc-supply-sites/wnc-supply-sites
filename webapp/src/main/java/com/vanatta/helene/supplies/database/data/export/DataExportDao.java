package com.vanatta.helene.supplies.database.data.export;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

class DataExportDao {

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

  static List<SiteExportData> fetchAllSites(Jdbi jdbi) {
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

  @Data
  @NoArgsConstructor
  public static class SiteItemExportData {
    String siteName;

    List<String> needed;
    List<String> available;

    SiteItemExportData(SiteItemResult result) {
      this.siteName = result.getSiteName();
      this.needed =
          result.getNeeded() == null ? List.of() : Arrays.asList(result.getNeeded().split(","));
      this.available =
          result.getAvailable() == null
              ? List.of()
              : Arrays.asList(result.getAvailable().split(","));
    }
  }

  @Data
  @NoArgsConstructor
  public static class SiteItemResult {
    String siteName;
    String needed;
    String available;
  }

  static List<SiteItemExportData> fetchAllSiteItems(Jdbi jdbi) {
    String query =
        """
        select
          s.name site_name,
          string_agg(i.name, ', ') filter (where its.name in ('Urgently Needed', 'Needed')) needed,
          string_agg(i.name, ', ') filter (where its.name in ('Available', 'Oversupply')) available
        from site s
        join site_item si on s.id = si.site_id
        join item i on i.id = si.item_id
        join item_status its on its.id = si.item_status_id
        group by site_name;
        """;
    return jdbi
        .withHandle(handle -> handle.createQuery(query).mapToBean(SiteItemResult.class).list())
        .stream()
        .map(SiteItemExportData::new)
        .toList();
  }
}
