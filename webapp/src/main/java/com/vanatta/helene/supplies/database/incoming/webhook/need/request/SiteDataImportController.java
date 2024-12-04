package com.vanatta.helene.supplies.database.incoming.webhook.need.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    public boolean isMissingData() {
      return airtableId == null
//          || wssId == null
          || siteName == null
          || streetAddress == null
          || city == null
          || county == null
          || state == null
          || donationStatus == null;
    }
  }

  @PostMapping("/import/update/site-data")
  ResponseEntity<String> updateSiteData(@RequestBody SiteUpdate siteUpdate) {
    if (siteUpdate.isMissingData()) {
      log.warn("DATA IMPORT (INCOMPLETE DATA), received site update: {}", siteUpdate);
      return ResponseEntity.badRequest().body("Missing data");
    }
    log.info("DATA IMPORT, received inventory site update: {}", siteUpdate);


    if(siteUpdate.getWssId() == null) {
      // this is an insert
    } else {
      // this is an update
    }

    return ResponseEntity.ok().build();
  }






}
