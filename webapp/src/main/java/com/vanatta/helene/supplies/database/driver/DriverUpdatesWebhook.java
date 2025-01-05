package com.vanatta.helene.supplies.database.driver;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@Slf4j
@AllArgsConstructor
public class DriverUpdatesWebhook {

  private final Jdbi jdbi;

  @PostMapping("/webhook/driver/upsert")
  ResponseEntity<String> receiveDriverUpdates(@RequestBody String driver) {
    log.info("Received driver upsert: {}", driver);
    Driver driverJson = Driver.parseJson(driver);
    try {
      DriverDao.upsert(jdbi, driverJson);
    } catch (Exception e) {
      if (e.getMessage().contains("duplicate key")) {
        log.warn("Duplicate driver received: {}", driver);
        return ResponseEntity.badRequest()
            .body("Duplicate driver, phone number already exists with another driver");
      } else {
        throw e;
      }
    }

    return ResponseEntity.ok("ok");
  }

  @PostMapping("/webhook/driver/update-field")
  ResponseEntity<String> receiveDriveFieldUpdate(@RequestBody String update) {
    log.info("Received driver field update: {}", update);

    DriverDao.DriverUpdate driverUpdate = DriverDao.DriverUpdate.parseJson(update);
    DriverDao.update(jdbi, driverUpdate);

    return ResponseEntity.ok("ok");
  }
}
