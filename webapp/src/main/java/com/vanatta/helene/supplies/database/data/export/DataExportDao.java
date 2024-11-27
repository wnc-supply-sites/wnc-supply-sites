package com.vanatta.helene.supplies.database.data.export;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

class DataExportDao {

  /** Data that can be sent as JSON to sevice. */
  @Data
  @NoArgsConstructor
  public static class SiteExportData {
    String siteName;
    String oldName;
    List<String> siteType;
    String contactNumber;
    String address;
    String city;
    String state;
    String county;
    String website;
    String donationStatus;
    boolean active;

    SiteExportData(SiteExportDataResult result) {
      this.siteName = result.getSiteName();
      oldName = this.siteName;
      this.siteType = Arrays.asList(result.getSiteType().split(","));
      this.contactNumber = result.getContactNumber();
      this.address = result.getAddress();
      this.city = result.getCity();
      this.state = result.getState();
      this.county = result.getCounty();
      this.website = result.getWebsite();
      this.donationStatus = result.getDonationStatus();
      this.active = result.isActive();
    }
  }

  /** Data value that comes from JSON */
  @Data
  @NoArgsConstructor
  public static class SiteExportDataResult {
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

  private static final String fetchSiteDataQuery =
      """
      select
        s.name siteName,
        case when st.name = 'Distribution Center' then 'POD,POC' else 'POD,POC,HUB' end siteType,
        s.contact_number,
        s.address,
        s.city,
        s.state,
        s.website,
        c.name county,
        case when not active
          then 'Closed'
          else case when s.accepting_donations then 'Accepting Donations' else 'Not Accepting Donations' end
        end donationStatus,
        s.active
      from site s
      join county c on c.id = s.county_id
      join site_type st on st.id = s.site_type_id
      """;

  static List<SiteExportData> fetchAllSites(Jdbi jdbi) {

    return jdbi
        .withHandle(
            handle ->
                handle.createQuery(fetchSiteDataQuery).mapToBean(SiteExportDataResult.class).list())
        .stream()
        .map(SiteExportData::new)
        .toList();
  }

  public static SiteExportData lookupSite(Jdbi jdbi, long siteId) {
    String fetchByIdQuery = fetchSiteDataQuery + "\nwhere s.id = :siteId";

    return new SiteExportData(
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(fetchByIdQuery)
                    .bind("siteId", siteId)
                    .mapToBean(SiteExportDataResult.class)
                    .one()));
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
