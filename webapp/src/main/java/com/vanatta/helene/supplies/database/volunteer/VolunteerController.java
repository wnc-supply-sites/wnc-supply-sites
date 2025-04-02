package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.DeploymentAdvice;

import java.util.*;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
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
  private final VolunteerService volunteerService;

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
      VolunteerService.VolunteerDelivery createdDelivery = volunteerService.createVolunteerDelivery(jdbi, request);
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
  ModelAndView deliveryPortal(
      @ModelAttribute(LoggedInAdvice.USER_PHONE) String userPhone,
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> userSites,
      @RequestParam String urlKey) {
    return deliveryPortal(jdbi, userPhone, userSites, urlKey);
  }

  /**
   * Checks if delivery exists and if the user is already logged in and is a site manager or the volunteer
   * Returns the urlKey and a boolean representing if the user required verification or not
   * */
  public static ModelAndView deliveryPortal(Jdbi jdbi, String userPhone, List<Long> userSites, String urlKey) {
    // Get Volunteer Delivery
    log.info("Received request for Volunteer Delivery {}", urlKey.toUpperCase());
    Optional<VolunteerService.VolunteerDeliveryRequest> deliveryRequestOpt = VolunteerService.getVolunteerDeliveryRequest(jdbi, urlKey.toUpperCase());

    Map<String, Object> pageParams = new HashMap<>();

    // If volunteer Delivery is not available reroute them to home
    // todo: create a 404 not found page to redirect to
    if (deliveryRequestOpt.isEmpty()) return new ModelAndView("redirect:/");

    VolunteerService.VolunteerDeliveryRequest deliveryRequest = deliveryRequestOpt.get();

    pageParams.put("urlKey", urlKey);

    // Check if user requires phone verification
    pageParams.put(
        "userRequiresPhoneAuth",
        !userSites.contains(deliveryRequest.siteId) && !Objects.equals(deliveryRequest.volunteerPhone, userPhone)
    );

    pageParams.put("userPhone",  userPhone == null ? "" : userPhone);

    return new ModelAndView("volunteer/delivery/request", pageParams);
  }

  /**
   * A site sends urlKey, phoneNumber, and volunteerSection
   * Verify that the phone number is associated with the delivery and returns access level and delivery data
   * If not then an 401 error is returned
   * */
  @PostMapping("/volunteer/verify-delivery")
  ResponseEntity<?> verifyAndRetrieveDelivery(@RequestBody VolunteerService.VerificationRequest body) {

    // Check access
    HashMap<String, Boolean> access = VolunteerService.verifyVolunteerPortalAccess(jdbi, body.urlKey, body.phoneNumber, "delivery");

    // If user does not hav access return 403 forbidden response
    if (access.isEmpty())  {
      log.info("Verification failed for volunteer volunteer delivery: {}", body.urlKey);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: User is not verified");
    };

    Optional<VolunteerService.VolunteerDeliveryRequest> deliveryRequestOpt = VolunteerService.getVolunteerDeliveryRequest(jdbi, body.getUrlKey());

    // Filter delivery request based on access
    if (deliveryRequestOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body("Volunteer delivery request not found for the provided URL key.");
    }

    VolunteerService.VolunteerDeliveryRequest deliveryRequest = deliveryRequestOpt.get();

    // Only shows volunteer and manager phone numbers if the request status is accepted
    HashMap<String, Object> requestInfo = deliveryRequest.scrubDataBasedOnStatus();

    HashMap<String, Object> response = new HashMap<>();
    response.put("access", access);
    response.put("request", requestInfo);

    return ResponseEntity.ok(response);
  }

}
