package com.vanatta.helene.supplies.database.driver;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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

  @Getter
  @ToString
  @EqualsAndHashCode
  static class DriverJson {
    private long airtableId;
    private String fullName;
    private String email;
    private String phone;
    private boolean active;
    private String location;

    static DriverJson parseJson(String json) {
      return new Gson().fromJson(json, DriverJson.class);
    }
  }

  @PostMapping("/webhook/driver/update")
  ResponseEntity<String> receiveDriverUpdates(@RequestBody String driver) {
    log.info("Received driver update: {}", driver);

    return ResponseEntity.ok("ok");
  }

  @GetMapping("/driver/portal")
  ModelAndView showDriverPortal() {

    return new ModelAndView("driver/portal");
  }
}
