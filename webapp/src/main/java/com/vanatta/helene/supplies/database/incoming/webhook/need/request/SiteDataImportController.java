package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.util.EnumUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class SiteDataImportController {

  private final Jdbi jdbi;

  @Value
  @Builder(toBuilder = true)
  @AllArgsConstructor
  public static class SiteUpdate {
    Long airtableId;
    Long wssId;
    String siteName;
    List<String> siteType;
    boolean publicVisibility;
    String donationStatus;
    String hours;
    String streetAddress;
    String city;
    String state;
    String county;
    String pointOfContact;
    String email;
    String phone;
    String website;
    String facebook;

    boolean isMissingData() {
      return airtableId == null
          || siteName == null
          || streetAddress == null
          || city == null
          || county == null
          || state == null
          || siteType == null;
    }

    /** Returns true if the site type list contains one of 'HUB' or 'POD' */
    SiteType getSiteType() {
      assert siteType != null;
      return siteType.contains("HUB") ? SiteType.SUPPLY_HUB : SiteType.DISTRIBUTION_CENTER;
    }

    boolean isDistributingSupplies() {
      return siteType.contains("POD");
    }
  }

  /** Models the donation statuses in Airtable. */
  @AllArgsConstructor
  @Getter
  enum DonationStatus {
    ACCEPTING_DONATIONS("Accepting Donations", true, true),
    ACCEPTING_REQUESTED_ONLY("Accepting Requested Donations Only", true, true),
    NOT_ACCEPTING_DONATIONS("Not Accepting Donations", false, true),
    CLOSED("Closed", false, false);
    private final String textValue;
    private final boolean acceptingDonations;
    private final boolean active;

    static DonationStatus fromText(String text) {
      return EnumUtil.mapText(values(), DonationStatus::getTextValue, text)
          .orElseThrow(() -> new IllegalArgumentException("Donation status not found: " + text));
    }
  }

  @PostMapping("/import/update/site-data")
  ResponseEntity<String> updateSiteData(@RequestBody SiteUpdate siteUpdate) {
    log.info("DATA IMPORT, received inventory site update: {}", siteUpdate);
    if (siteUpdate.isMissingData()) {
      log.warn("DATA IMPORT (INCOMPLETE DATA), received site update: {}", siteUpdate);
      return ResponseEntity.badRequest().body("Missing data");
    }
    insertStateCountyIfDoesNotExist(jdbi, siteUpdate.county, siteUpdate.state);

    if (siteUpdate.getWssId() == null) {
      // this is an insert
      insert(jdbi, siteUpdate);
      log.info("DATA IMPORT: successfully inserted data: {}", siteUpdate);
      // now send update to Airtable to send Airtable our WSS ID
    } else {
      // this is an update
      update(jdbi, siteUpdate);
      log.info("DATA IMPORT: successfully updated data: {}", siteUpdate);
    }

    return ResponseEntity.ok().build();
  }

  private static void insertStateCountyIfDoesNotExist(Jdbi jdbi, String county, String state) {
    String select = "select 1 from county where name = :county and state = :state";
    boolean exists =
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(select)
                        .bind("county", county)
                        .bind("state", state)
                        .mapTo(Long.class)
                        .findOne())
            .isPresent();
    if (!exists) {
      String insert = "insert into county(name, state) values(:county, :state)";
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(insert) //
                  .bind("county", county)
                  .bind("state", state)
                  .execute());
    }
  }

  private static void insert(Jdbi jdbi, SiteUpdate input) {
    assert input.getWssId() == null;

    String insert =
        """
        insert into site(name, address, city, accepting_donations, active,
          contact_number, website, airtable_id, hours, contact_name, facebook,
          contact_email, publicly_visible, distributing_supplies,
          county_id,
          site_type_id)
        values(
          :name, :address, :city, :acceptingDonations, :active,
          :contactNumber, :website, :airtableId, :hours, :contactName, :facebook,
          :contactEmail, :publiclyVisible, :distributingSupplies,
          (select id from county where name = :county and state = :state),
          (select id from site_type where name = :siteType)
        )
        """;

    jdbi.withHandle(handle -> doBindings(handle.createUpdate(insert), input).execute());
  }

  private static void update(Jdbi jdbi, SiteUpdate input) {
    assert input.getWssId() != null;
    String update =
        """
        update site set
          name = :name,
          address = :address,
          city = :city,
          accepting_donations = :acceptingDonations,
          distributing_supplies = :distributingSupplies,
          active = :active,
          contact_number = :contactNumber,
          website = :website,
          hours = :hours,
          contact_name = :contactName,
          facebook = :facebook,
          contact_email = :contactEmail,
          publicly_visible = :publiclyVisible,
          airtable_id = :airtableId,
          county_id = (select id from county where name = :county and state = :state),
          site_type_id = (select id from site_type where name = :siteType)
        where wss_id = :wssId
        """;
    jdbi.withHandle(
        handle -> {
          Update statement = handle.createUpdate(update).bind("wssId", input.getWssId());
          return doBindings(statement, input).execute();
        });
  }

  private static Update doBindings(Update sqlStatement, SiteUpdate input) {
    DonationStatus donationStatus = DonationStatus.fromText(input.getDonationStatus());
    return sqlStatement
        .bind("airtableId", input.getAirtableId())
        .bind("name", input.getSiteName())
        .bind("address", input.getStreetAddress())
        .bind("city", input.getCity())
        .bind("acceptingDonations", donationStatus.acceptingDonations)
        .bind("distributingSupplies", input.isDistributingSupplies())
        .bind("active", donationStatus.active)
        .bind("contactNumber", input.getPhone())
        .bind("website", input.getWebsite())
        .bind("hours", input.getHours())
        .bind("contactName", input.getPointOfContact())
        .bind("facebook", input.getFacebook())
        .bind("contactEmail", input.getEmail())
        .bind("publiclyVisible", input.isPublicVisibility())
        .bind("county", input.getCounty())
        .bind("state", input.getState())
        .bind("siteType", input.getSiteType().getText());
  }
}
