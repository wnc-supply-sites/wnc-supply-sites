package com.vanatta.helene.supplies.database.driver;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@AllArgsConstructor
public class DriverController {

  private final Jdbi jdbi;

  @PostMapping("/webhook/driver/update")
  ResponseEntity<String> receiveDriverUpdates(@RequestBody String driver) {
    log.info("Received driver update: {}", driver);

    DriverDao.Driver driverJson = DriverDao.Driver.parseJson(driver);
    DriverDao.upsert(jdbi, driverJson);

    return ResponseEntity.ok("ok");
  }

  @GetMapping("/driver/portal")
  ModelAndView showDriverPortal() {

    return new ModelAndView("driver/portal");
  }
}
