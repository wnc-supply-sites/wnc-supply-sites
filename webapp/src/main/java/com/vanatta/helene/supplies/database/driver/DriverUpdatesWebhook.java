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

//    DriverDao.Driver driverJson = DriverDao.Driver.parseJson(driver);
//    DriverDao.upsert(jdbi, driverJson);
    
    return ResponseEntity.ok("ok");
  }
  
  @PostMapping("/webhook/driver/update-field")
  ResponseEntity<String> receiveDriveFieldUpdate(@RequestBody String update) {
    log.info("Received driver field update: {}", update);

//    DriverDao.Driver driverJson = DriverDao.Driver.parseJson(driver);
//    DriverDao.upsert(jdbi, driverJson);
    
    return ResponseEntity.ok("ok");
  }
  
  
  
}
