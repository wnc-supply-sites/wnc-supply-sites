package com.vanatta.helene.supplies.database.export.bulk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

public class BulkDataExportDao {

  public static List<String> getAllItems(Jdbi jdbi) {
    String query =
        """
          select name from item order by lower(name)
        """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(String.class).list());
  }

  public static List<SiteExportJson> fetchAllSites(Jdbi jdbi) {
    String fetchSiteDataQuery =
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
          s.active,
          string_agg(i.name, ',') filter (where its.name in ('Urgently Needed')) urgentlyNeeded,
          string_agg(i.name, ',') filter (where its.name in ('Needed')) needed,
          string_agg(i.name, ',') filter (where its.name in ('Available')) available,
          string_agg(i.name, ',') filter (where its.name in ('Oversupply')) oversupply
        from site s
        join county c on c.id = s.county_id
        join site_type st on st.id = s.site_type_id
        left join site_item si on s.id = si.site_id
        left join item i on i.id = si.item_id
        left join item_status its on its.id = si.item_status_id
        """;

    return jdbi
        .withHandle(
            handle -> handle.createQuery(fetchSiteDataQuery).mapToBean(SiteDataResult.class).list())
        .stream()
        .map(SiteExportJson::new)
        .toList();
  }

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
    List<String> urgentlyNeededItems;
    List<String> neededItems;
    List<String> availableItems;
    List<String> oversupplyItems;

    SiteExportJson(SiteDataResult result) {
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

      this.urgentlyNeededItems = extractField(result, SiteDataResult::getUrgentlyNeeded);
      this.neededItems = extractField(result, SiteDataResult::getNeeded);
      this.availableItems = extractField(result, SiteDataResult::getAvailable);
      this.oversupplyItems = extractField(result, SiteDataResult::getOverSupply);
    }

    private static List<String> extractField(
        SiteDataResult result, Function<SiteDataResult, String> mapping) {
      String value = mapping.apply(result);
      return value == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value.split(",")));
    }
  }

  /** Represents DB data for one site. */
  @Data
  @NoArgsConstructor
  public static class SiteDataResult {
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

    /** Items are encoded as a comma delimited list */
    String urgentlyNeeded;

    String needed;
    String available;
    String overSupply;
  }
}
