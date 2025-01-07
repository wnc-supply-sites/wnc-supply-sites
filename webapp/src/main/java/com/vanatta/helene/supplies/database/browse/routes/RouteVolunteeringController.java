package com.vanatta.helene.supplies.database.browse.routes;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import com.vanatta.helene.supplies.database.driver.DriverDao;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Handles driver volunteer requests for deliveries from the route-browser page. */
@Controller
@Slf4j
@AllArgsConstructor
public class RouteVolunteeringController {

  private final Jdbi jdbi;
  private final SendVolunteerRequest sendVolunteerRequest;

  @PostMapping("/browse/routes/volunteer")
  ResponseEntity<String> volunteer(
      @RequestBody DeliveryVolunteerRequest params,
      @ModelAttribute(LoggedInAdvice.USER_PHONE) String driverPhone) {
    log.info("/browse/routes/volunteer - Received driver volunteer request: {}", params);

    DeliveryVolunteerRequest json = createVolunteeringRequestJson(jdbi, params, driverPhone);
    log.info("Sending volunteer request to airtable: {}", json);
    try {
      sendVolunteerRequest.send(json);
      return ResponseEntity.ok("{\"result\": \"ok\"}");
    } catch (Exception e) {
      log.error("Error sending volunteering request to server: {}", json, e);
      return ResponseEntity.internalServerError()
          .body(String.format("{\"error\": \"%s\"}", e.getMessage()));
    }
  }

  static DeliveryVolunteerRequest createVolunteeringRequestJson(
      Jdbi jdbi, DeliveryVolunteerRequest volunteerRequest, String driverPhone) {
    return volunteerRequest.toBuilder()
        .driverAirtableId(DriverDao.lookupByPhone(jdbi, driverPhone).orElseThrow().getAirtableId())
        .build();
  }

  @Builder(toBuilder = true)
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DeliveryVolunteerRequest {
    long fromSiteWssId;
    long toSiteWssId;
    List<Long> itemList;
    String fromDate;
    String toDate;
    long driverAirtableId;
  }
}
