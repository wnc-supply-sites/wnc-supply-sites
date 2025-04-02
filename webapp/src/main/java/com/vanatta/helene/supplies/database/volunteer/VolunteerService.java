package com.vanatta.helene.supplies.database.volunteer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.vanatta.helene.supplies.database.util.URLKeyGenerator.generateUrlKey;
import static com.vanatta.helene.supplies.database.volunteer.VolunteerDao.*;

@Service
@Slf4j
public class VolunteerService {

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteSelect {
    Long id;
    String name;
    String county;
    String state;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Item {
    Long id;
    String name;
    String status;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Site {
    Long id;
    String name;
    String address;
    String county;
    String state;
    List<Item> items;
  }

  @Data
  @AllArgsConstructor
  @Builder
  public static class DeliveryForm {
    List<Long> neededItems;
    String site;
    String volunteerContact;
    String volunteerName;
    String urlKey;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class VolunteerDelivery {
    Long id;
    String volunteerName;
    String volunteerPhone;
    Long siteId;
    String urlKey;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VolunteerDeliveryItem {
    Long id;
    Long site_item_id;
    Long volunteer_delivery_id;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VolunteerDeliveryRequestItem {
    String name;
    String status;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VolunteerDeliveryRequest {
    Long id;
    String status;
    String siteName;
    Long siteId;
    String urlKey;
    String address;
    String city;
    String volunteerName;
    String volunteerPhone;
    String siteContactNumber;
    String siteContactName;
    List<VolunteerDeliveryRequestItem> items;

    public void insertItems(List<VolunteerDeliveryRequestItem> deliveryItems) {
      this.items = deliveryItems;
    }

    public HashMap<String, Object> scrubDataBasedOnStatus(){
      HashMap<String, Object> scrubbedData = new HashMap<>();
      scrubbedData.put("status", this.status);
      scrubbedData.put("urlKey", this.urlKey);

      // Provide site name, volunteerName, site address, siteId, and items is request is still active (pending/accepted);
      if (!Objects.equals(this.status, "DECLINED") || !Objects.equals(this.status, "CANCELLED")) {
        scrubbedData.put("siteName", this.siteName);
        scrubbedData.put("volunteerName", this.volunteerName);
        scrubbedData.put("address", this.address);
        scrubbedData.put("siteId", this.siteId);
        scrubbedData.put("items", this.items);
      }

      // Provided user info and site contact if accepted
      if (Objects.equals(this.status, "ACCEPTED")) {
        scrubbedData.put("city", this.city);
        scrubbedData.put("volunteerPhone", this.volunteerPhone);
        scrubbedData.put("siteContactNumber", this.siteContactNumber);
        scrubbedData.put("siteContactName", this.siteContactName);
      };
      return scrubbedData;
    }

    public String getCleanedSitePhoneNumber() {
      return this.siteContactNumber.replaceAll("[^0-9]", "");
    }

    public String getCleanedVolunteerPhoneNumber() {
      return this.volunteerPhone.replaceAll("[^0-9]", "");
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VerificationRequest {
    String phoneNumber;
    String urlKey;
    String section;
  }

  public VolunteerService.VolunteerDelivery createVolunteerDelivery(Jdbi jdbi, DeliveryForm request) {
    Handle handle = jdbi.open();
    try {
      handle.begin();

      // Remove '-' from phone number
      request.volunteerContact = String.join("", request.volunteerContact.split("-"));

      // Add urlKey
      request.urlKey = generateUrlKey();

      Long volunteerDeliveryId = VolunteerDao.createVolunteerDelivery(handle.getJdbi(), request);
      createVolunteerDeliveryItems(handle.getJdbi(), volunteerDeliveryId, request.getNeededItems());
      handle.commit();
      log.info("Created volunteer delivery in DB of ID: {}", volunteerDeliveryId);

      return VolunteerService.getDeliveryById(jdbi, volunteerDeliveryId);
    } catch (Exception e) {
      handle.rollback();
      log.error("Error while creating volunteer delivery. Transaction rolled back.", e);
      throw new RuntimeException("Error while creating volunteer delivery. Rolling back.", e);
    } finally {
      handle.close();
    }
  }

  /** Grabs a volunteer delivery request via ID */
  public static VolunteerDelivery getDeliveryById(Jdbi jdbi, Long id) {
    try {
      return VolunteerDao.getVolunteerDeliveryById(jdbi, id);
    } catch (Exception e) {
      log.error("Error while looking up delivery by id: ", e);
      throw new RuntimeException("Error while looking up delivery by id: ", e);
    }
  }

  /** Grabs a volunteer delivery request via urlKey and returns an optional. */
  public static Optional<VolunteerDeliveryRequest> getVolunteerDeliveryRequest(Jdbi jdbi, String urlKey) {
    try {
      Optional<VolunteerDeliveryRequest> volunteerDeliveryRequestOpt =  getVolunteerDeliveryByUrlKey(jdbi, urlKey);

      if (volunteerDeliveryRequestOpt.isEmpty()) return volunteerDeliveryRequestOpt;

      VolunteerDeliveryRequest volunteerDeliveryRequest = volunteerDeliveryRequestOpt.get();

      List<VolunteerDeliveryRequestItem> deliveryItems = VolunteerDao.getVolunteerDeliveryItems(jdbi, volunteerDeliveryRequest.getId());

      volunteerDeliveryRequest.insertItems(deliveryItems);

      return Optional.of(volunteerDeliveryRequest);
    } catch (Exception e) {
      log.error("Error while looking up delivery by urlKey: ", e);
      throw new RuntimeException("Error while looking up delivery by urlKey: ", e);
    }
  }

  /** Determines which delivery type this verify request is for and calls the correct verifyer function */
  public static HashMap<String, Boolean> verifyVolunteerPortalAccess(Jdbi jdbi, String urlKey, String phoneNumber, String section){
    switch (section) {
      // Grab delivery request, cleans numbers, and calls correct verifyer function
      case "delivery":
        Optional<VolunteerDeliveryRequest> deliveryRequestOpt = VolunteerDao.getVolunteerDeliveryByUrlKey(jdbi, urlKey);
        if (deliveryRequestOpt.isEmpty()) return new HashMap<>();
        VolunteerDeliveryRequest deliveryRequest = deliveryRequestOpt.get();

        return verifyDeliveryPortalAccess(phoneNumber, deliveryRequest);
      default:
        return new HashMap<>();
    }
  }

  /** Gets called by verifyVolunteerPortalAccess. Determines if a user is a volunteer or manager or both. If not verified return an empty HashMap */
  private static HashMap<String, Boolean> verifyDeliveryPortalAccess(String userPhoneNumber, VolunteerDeliveryRequest deliveryRequest) {
    String cleanedUserPhoneNumber = userPhoneNumber.replaceAll("[^0-9]", "");

    Boolean hasVolunteerAccess = Objects.equals(deliveryRequest.getCleanedVolunteerPhoneNumber(), cleanedUserPhoneNumber);
    Boolean hasSiteManagerAccess = Objects.equals(deliveryRequest.getCleanedSitePhoneNumber(), cleanedUserPhoneNumber);

    HashMap<String, Boolean> access = new HashMap<>();

    // Returns an empty hashmap if not verified
    if (!hasVolunteerAccess & !hasSiteManagerAccess) {
      return access;
    }

    access.put("hasManagerAccess", hasSiteManagerAccess);
    access.put("hasVolunteerAccess", hasVolunteerAccess);
    return access;
  }
}


































