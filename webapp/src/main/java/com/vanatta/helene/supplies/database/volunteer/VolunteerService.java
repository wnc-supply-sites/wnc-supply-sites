package com.vanatta.helene.supplies.database.volunteer;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Service;

import static com.vanatta.helene.supplies.database.volunteer.VolunteerDao.createVolunteerDeliveryItems;

@Service
@Slf4j
public class VolunteerService {
  public Long createVolunteerDelivery(Jdbi jdbi, VolunteerController.DeliveryForm request) {
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
}
