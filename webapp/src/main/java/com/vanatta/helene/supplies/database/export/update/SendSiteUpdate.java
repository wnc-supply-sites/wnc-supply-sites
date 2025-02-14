package com.vanatta.helene.supplies.database.export.update;

import com.vanatta.helene.supplies.database.data.DonationStatus;
import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.util.HttpPostSender;
import com.vanatta.helene.supplies.database.util.ThreadRunner;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/**
 * Sends site updates "Make". For example, if a site updates its contact info, website, address. On
 * any such update, we send a full snapshot of the site's data.
 */
@Slf4j
@AllArgsConstructor
public class SendSiteUpdate {

  private final Jdbi jdbi;
  private final String webhookUrl;
  private final boolean enabled;

  // @VisibleForTesting
  public static SendSiteUpdate newDisabled() {
    return new SendSiteUpdate(null, null, false);
  }

  static long fetchWssIdByAirtableId(Jdbi jdbi, long airtableId) {
    String query = "select wss_id from site where airtable_id = :airtableId";
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(query) //
                .bind("airtableId", airtableId)
                .mapTo(Long.class)
                .one());
  }

  public void sendFullUpdate(long siteId) {
    if (!enabled) {
      return;
    }
    ThreadRunner.run(
        () -> {
          var siteExportJson = lookupSite(jdbi, siteId);
          HttpPostSender.sendAsJson(webhookUrl, siteExportJson);
        });
  }

  static SiteExportJson lookupSite(Jdbi jdbi, long siteId) {
    String fetchByIdQuery =
        """
            select
              s.wss_id,
              s.airtable_id,
              s.name siteName,
              st.name siteType,
              s.contact_number,
              s.contact_name,
              s.address,
              s.city,
              c.state,
              c.name county,
              s.website,
              s.facebook,
              s.accepting_donations,
              s.distributing_supplies,
              s.active,
              s.publicly_visible,
              s.hours,
              s.additional_contacts,
              msl.name maxSupplyTruckSize,
              s.inactive_reason,
              s.weekly_served
            from site s
            join county c on c.id = s.county_id
            join site_type st on st.id = s.site_type_id
            join max_supply_load msl on msl.id = s.max_supply_load_id
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
  @Builder(toBuilder = true)
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteExportDataResult {
    String wssId;
    String airtableId;

    String siteName;
    String siteType;
    String address;
    String city;
    String state;
    String county;

    String contactNumber;
    String contactName;
    String additionalContacts;
    String website;
    String facebook;
    String hours;

    boolean active;
    boolean publiclyVisible;
    boolean acceptingDonations;
    boolean distributingSupplies;

    String maxSupplyTruckSize;

    String inactiveReason;

    Number weeklyServed;
  }

  /** JSON representation, to be sent externally. */
  @Builder
  @Value
  @AllArgsConstructor
  public static class SiteExportJson {
    String wssId;
    String airtableId;
    String siteName;
    List<String> siteTypes;
    String contactNumber;
    String contactName;
    String additionalContacts;
    String address;
    String city;
    String state;
    String county;
    String website;
    String facebook;
    String donationStatus;
    String hours;
    boolean active;
    boolean publiclyVisible;

    String maxSupplyTruckSize;
    String inactiveReason;
    Number weeklyServed;

    SiteExportJson(SiteExportDataResult result) {
      this.wssId = result.wssId;
      this.airtableId = result.airtableId;
      this.siteName = result.getSiteName();

      this.siteTypes = new ArrayList<>();
      if (result.isAcceptingDonations()) {
        siteTypes.add("POC");
      }
      if (result.isDistributingSupplies()) {
        siteTypes.add("POD");
      }
      if (result.siteType.equals(SiteType.SUPPLY_HUB.getText())) {
        siteTypes.add("HUB");
      }
      this.contactNumber = result.getContactNumber();
      this.contactName = result.getContactName();
      this.additionalContacts = result.getAdditionalContacts();
      this.address = result.getAddress();
      this.city = result.getCity();
      this.state = result.getState();
      this.county = result.getCounty();
      this.website = result.getWebsite();
      this.facebook = result.getFacebook();
      this.active = result.isActive();
      this.publiclyVisible = result.isPubliclyVisible();
      this.hours = result.getHours();

      if (!result.isActive()) {
        this.donationStatus = DonationStatus.CLOSED.getTextValue();
      } else if (result.isAcceptingDonations()) {
        this.donationStatus = DonationStatus.ACCEPTING_DONATIONS.getTextValue();
      } else {
        this.donationStatus = DonationStatus.NOT_ACCEPTING_DONATIONS.getTextValue();
      }

      this.maxSupplyTruckSize = result.getMaxSupplyTruckSize();
      this.inactiveReason = result.getInactiveReason();
      this.weeklyServed = result.getWeeklyServed();
    }
  }
}
