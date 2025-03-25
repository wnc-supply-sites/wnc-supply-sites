package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.DeploymentAdvice;

import java.util.*;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
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

  /** Users will be shown a form to request to make a delivery */
  @GetMapping("/volunteer/delivery")
  ModelAndView deliveryForm(
      @ModelAttribute(DeploymentAdvice.DEPLOYMENT_STATE_LIST) List<String> states) {
    return deliveryForm(jdbi, states);
  }

  public static ModelAndView deliveryForm(Jdbi jdbi, List<String> states) {
    Map<String, Object> pageParams = new HashMap<>();

    List<VolunteerService.SiteSelect> sites = VolunteerDao.fetchSiteSelect(jdbi, states);

    pageParams.put("sites", sites);
    return new ModelAndView("volunteer/delivery-form", pageParams);
  }

  /** Adds volunteer request to DB */
  @PostMapping("/volunteer/delivery")
  ResponseEntity<String> submitDeliveryRequest(@RequestBody VolunteerService.DeliveryForm request) {
    log.info("Received delivery request for site: {}", request.site);

    try {
      // Remove '-' from phone number
      request.volunteerContact = String.join("", request.volunteerContact.split("-"));

      // Add urlKey
      request.urlKey = generateUrlKey();

      Long deliveryId = volunteerService.createVolunteerDelivery(jdbi, request);
      VolunteerService.VolunteerDelivery createdDelivery = VolunteerService.getDeliveryById(jdbi, deliveryId);

      return ResponseEntity.ok(createdDelivery.urlKey);

    } catch (Exception e) {
      log.error(e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to create delivery: " + e.getMessage());
    }
  }

  /** Return json payload of site info */
  @GetMapping("/volunteer/site-items")
  ResponseEntity<?> getSiteItems(@RequestParam String siteId) {
    VolunteerService.Site site = VolunteerDao.fetchSiteItems(jdbi, Long.parseLong(siteId));
    return ResponseEntity.ok(Map.of("site", site));
  }

  @GetMapping("/volunteer/delivery/request")
  ModelAndView volunteerDeliveryStatus(
      @ModelAttribute(LoggedInAdvice.USER_PHONE) String userPhone,
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> userSites,
      @RequestParam String urlKey) {
    return volunteerDeliveryStatus(jdbi, userPhone, userSites, urlKey);
  }

  public static ModelAndView volunteerDeliveryStatus(Jdbi jdbi, String userPhone, List<Long> userSites, String urlKey) {
    // Get Volunteer Delivery
    log.info("Received request for Volunteer Delivery {}", urlKey.toUpperCase());
    Optional<VolunteerService.VolunteerDeliveryRequest> deliveryRequestOpt = VolunteerService.getVolunteerDeliveryRequest(jdbi, urlKey.toUpperCase());

    Map<String, Object> pageParams = new HashMap<>();

    // If volunteer Delivery is not available reroute them to home
    // todo: create a 404 not found page to redirect to
    if (deliveryRequestOpt.isEmpty()) return new ModelAndView("redirect:/");

    VolunteerService.VolunteerDeliveryRequest deliveryRequest = deliveryRequestOpt.get();
    log.info("Received delivery request: {}", deliveryRequest);

    // Add retrieved Delivery data
    pageParams.put(
        "deliveryRequest",
        deliveryRequest
    );

    // Check if userPhone matches deliveryRequestPhone
    pageParams.put(
        "userIsVolunteer",
        Objects.equals(deliveryRequest.volunteerPhone, userPhone)
    );

    // Check if user Is Primary or Secondary Site Manager
    pageParams.put(
        "userIsManager",
        userSites.contains(deliveryRequest.siteId)
    );

    // Check if user requires phone verification
    pageParams.put(
        "userRequiresPhoneAuth",
        !userSites.contains(deliveryRequest.siteId) && !Objects.equals(deliveryRequest.volunteerPhone, userPhone)
    );

    return new ModelAndView("volunteer/delivery/request", pageParams);
  }
}
