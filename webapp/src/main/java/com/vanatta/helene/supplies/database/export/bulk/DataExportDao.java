package com.vanatta.helene.supplies.database.export.bulk;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class DataExportDao {

  /** Data that can be sent as JSON to sevice. */
  @Data
  @NoArgsConstructor
  public static class SiteExportJson {
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

    SiteExportJson(SiteExportDataResult result) {
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

  public static List<SiteExportJson> fetchAllSites(Jdbi jdbi) {

    return jdbi
        .withHandle(
            handle ->
                handle.createQuery(fetchSiteDataQuery).mapToBean(SiteExportDataResult.class).list())
        .stream()
        .map(SiteExportJson::new)
        .toList();
  }

  public static SiteExportJson lookupSite(Jdbi jdbi, long siteId) {
    String fetchByIdQuery = fetchSiteDataQuery + "\nwhere s.id = :siteId";

    return new SiteExportJson(
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(fetchByIdQuery)
                    .bind("siteId", siteId)
                    .mapToBean(SiteExportDataResult.class)
                    .one()));
  }

  /**
   * Class that can be converted to a JSON. Input are results from DB, which have comma delimited
   * values, we put those into lists.
   */
  @Data
  @NoArgsConstructor
  public static class SiteItemExportJson {
    String siteName;

    //    List<String> urgentlyNeeded;
    List<String> needed;
    List<String> available;

    //    List<String> oversupply;

    /**
     * Constructor for case where site has no inventory.
     *
     * @param siteName the name of the site.
     */
    SiteItemExportJson(String siteName) {
      this.siteName = siteName;
      this.needed = new ArrayList<>();
      this.available = new ArrayList<>();
    }

    SiteItemExportJson(SiteItemResult result) {
      this.siteName = result.getSiteName();
      //      this.urgentlyNeeded = extractField(result, SiteItemResult::getUrgentlyNeeded);
      this.needed = extractField(result, SiteItemResult::getNeeded);
      if (result.getUrgentlyNeeded() != null) {
        needed.addAll(Arrays.asList(result.getUrgentlyNeeded().split(",")));
      }
      this.available = extractField(result, SiteItemResult::getAvailable);
      if (result.getOverSupply() != null) {
        available.addAll(Arrays.asList(result.getOverSupply().split(",")));
      }
      //      this.oversupply = extractField(result, SiteItemResult::getOverSupply);
    }

    private static List<String> extractField(
        SiteItemResult result, Function<SiteItemResult, String> mapping) {
      String value = mapping.apply(result);
      return value == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value.split(",")));
    }
  }

  /**
   * The DB result has 'needed'/'available'/... as comma delimited fields, not as a list. We need to
   * split those values into lists before we can send the data as a JSOn.
   */
  @Data
  @NoArgsConstructor
  public static class SiteItemResult {
    String siteName;
    String urgentlyNeeded;
    String needed;
    String available;
    String overSupply;
  }

  static List<SiteItemExportJson> fetchAllSiteItems(Jdbi jdbi) {
    String query = buildFetchInventoryQuery(null);
    return jdbi
        .withHandle(handle -> handle.createQuery(query).mapToBean(SiteItemResult.class).list())
        .stream()
        .map(SiteItemExportJson::new)
        .toList();
  }

  /**
   * Builds a query to fetch inventory for all sites. If provided siteId parameter is not null, then
   * query is for just one site.
   */
  private static String buildFetchInventoryQuery(@Nullable Long siteId) {
    String query =
        """
          select
            s.name site_name,
            string_agg(i.name, ', ') filter (where its.name in ('Urgently Needed')) urgentlyNeeded,
            string_agg(i.name, ', ') filter (where its.name in ('Needed')) needed,
            string_agg(i.name, ', ') filter (where its.name in ('Available')) available,
            string_agg(i.name, ', ') filter (where its.name in ('Oversupply')) oversupply
          from site s
          join site_item si on s.id = si.site_id
          join item i on i.id = si.item_id
          join item_status its on its.id = si.item_status_id
        """;
    if (siteId != null) {
      query += "\nwhere s.id = :siteId";
    }
    query += "\ngroup by site_name";
    return query;
  }

  public static SiteItemExportJson fetchAllSiteItemsForSite(Jdbi jdbi, long siteId) {
    String query = buildFetchInventoryQuery(siteId);
    var dbResult =
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(query)
                        .bind("siteId", siteId)
                        .mapToBean(SiteItemResult.class)
                        .findOne())
            .map(SiteItemExportJson::new)
            .orElse(null);
    if (dbResult != null) {
      return dbResult;
    }

    // site has no inventory, we need to look it up to find it's name and then return empty data
    String nameLookupQuery = "select name from site where id = :siteId";
    String siteName =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(nameLookupQuery)
                    .bind("siteId", siteId)
                    .mapTo(String.class)
                    .one());
    return new SiteItemExportJson(siteName);
  }
}
