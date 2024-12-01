package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.util.HttpPostSender;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/**
 * Sends site updates "Make". For example, if a site updates its contact info, website, address. On
 * any such update, we send a full snapshot of the sites data.
 */
@Slf4j
@AllArgsConstructor
public class SendSiteUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  public void sendWithNameUpdate(long siteId, String oldName) {
    if (!enabled) {
      return;
    }
    new Thread(
            () -> {
              SiteExportJson siteExportJson = lookupSite(jdbi, siteId);
              siteExportJson.setOldName(oldName);
              HttpPostSender.sendAsJson(webhookUrl, siteExportJson);
            })
        .start();
  }

  public void send(long siteId) {
    if (!enabled) {
      return;
    }
    new Thread(
            () -> {
              var siteExportJson = lookupSite(jdbi, siteId);
              siteExportJson.setOldName(siteExportJson.getSiteName());
              HttpPostSender.sendAsJson(webhookUrl, siteExportJson);
            })
        .start();
  }

  static SiteExportJson lookupSite(Jdbi jdbi, long siteId) {
    String fetchByIdQuery =
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
            where s.id = :siteId
            """;

    var result =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(fetchByIdQuery)
                    .bind("siteId", siteId)
                    .mapToBean(SiteExportDataResult.class)
                    .one());
    return new SiteExportJson(result);
  }

  /** Data from DB for a single site. */
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

  /** JSON representation, to be sent externally. */
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
}
