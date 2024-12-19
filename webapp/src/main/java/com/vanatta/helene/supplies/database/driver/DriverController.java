package com.vanatta.helene.supplies.database.driver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class DriverController {

  @PostMapping("/webhook/driver/update")
  ResponseEntity<String> receiveDriverUpdates(@RequestBody String driver) {
    log.info("Received driver update: {}", driver);
    return ResponseEntity.ok("ok");
  }
}
