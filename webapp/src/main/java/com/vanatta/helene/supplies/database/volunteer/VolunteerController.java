package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.DeploymentAdvice;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import static com.vanatta.helene.supplies.database.util.URLKeyGenerator.generateUrlKey;

@Controller
@AllArgsConstructor
@Slf4j
public class VolunteerController {
  private final Jdbi jdbi;

  private VolunteerService volunteerService;

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

  /** Users will be shown a form to request to make a delivery */
  @GetMapping("/volunteer/delivery")
  ModelAndView deliveryForm(
      @ModelAttribute(DeploymentAdvice.DEPLOYMENT_STATE_LIST) List<String> states) {
    return deliveryForm(jdbi, states);
  }

  public static ModelAndView deliveryForm(Jdbi jdbi, List<String> states) {
    Map<String, Object> pageParams = new HashMap<>();

    List<SiteSelect> sites = VolunteerDao.fetchSiteSelect(jdbi, states);

    pageParams.put("sites", sites);
    return new ModelAndView("volunteer/delivery-form", pageParams);
  }

  /** Return json payload of site info */
  @GetMapping("/volunteer/site-items")
  ResponseEntity<?> getSiteItems(@RequestParam String siteId) {
    Site site = VolunteerDao.fetchSiteItems(jdbi, Long.parseLong(siteId));
    return ResponseEntity.ok(Map.of("site", site));
  }

  /** Adds volunteer request to DB */
  @PostMapping("/volunteer/delivery")
  ResponseEntity<String> submitDeliveryRequest(@RequestBody DeliveryForm request) {
    log.info("Received delivery request for site: {}", request.site);

    try {
      // Remove '-' from phone number
      request.volunteerContact = String.join("", request.volunteerContact.split("-"));

      // Add urlKey
      request.urlKey = generateUrlKey();

      Long deliveryId = volunteerService.createVolunteerDelivery(jdbi, request);
    } catch (Exception e) {
      log.error(e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to create delivery: " + e.getMessage());
    }

    // todo: Should return the URLKey
    return ResponseEntity.ok("Volunteer request added successfully!");
  }
}
