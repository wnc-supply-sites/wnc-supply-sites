package com.vanatta.helene.supplies.database.volunteer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    Long id;
    String item_name;
    String item_status;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class VolunteerDeliveryRequest {
    Long id;
    String volunteerName;
    String volunteerPhone;
    String status;
    Long siteId;
    String urlKey;
    String address;
    String city;
    String siteContactNumber;
    String siteContactName;
    List<VolunteerDeliveryRequestItem> items;

    public void insertItems(List<VolunteerDeliveryRequestItem> deliveryItems) {
      this.items = deliveryItems;
    }
  }


  public Long createVolunteerDelivery(Jdbi jdbi, DeliveryForm request) {
    Handle handle = jdbi.open();
    try {
      handle.begin(); // Start the transaction

      // Create the volunteer delivery
      Long volunteerDeliveryId = VolunteerDao.createVolunteerDelivery(handle.getJdbi(), request);

      // Create the volunteer delivery items
      createVolunteerDeliveryItems(handle.getJdbi(), volunteerDeliveryId, request.getNeededItems());

      handle.commit(); // Commit the transaction
      log.info("Created volunteer delivery in DB of ID: {}", volunteerDeliveryId);
      return volunteerDeliveryId;
    } catch (Exception e) {
      handle.rollback(); // Rollback the transaction if an error occurs
      log.error("Error while creating volunteer delivery. Transaction rolled back.", e);
      throw new RuntimeException("Error while creating volunteer delivery. Rolling back.", e);
    } finally {
      handle.close(); // Ensure the handle is closed
    }
  }

  public static VolunteerDelivery getDeliveryById(Jdbi jdbi, Long id) {
    try {
      return VolunteerDao.getVolunteerDeliveryById(jdbi, id);
    } catch (Exception e) {
      log.error("Error while looking up delivery by id: ", e);
      throw new RuntimeException("Error while looking up delivery by id: ", e);
    }
  }

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

}


































