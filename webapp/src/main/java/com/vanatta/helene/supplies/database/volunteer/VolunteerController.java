package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.DeploymentAdvice;

import java.util.*;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import com.vanatta.helene.supplies.database.twilio.sms.SmsSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class VolunteerController {
  private final Jdbi jdbi;
  private final VolunteerService volunteerService;
  private final SmsSender smsSender;

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

  /**
   * Create Volunteer Delivery and adds it to the DB
   */
  @PostMapping("/volunteer/delivery")
  ResponseEntity<String> submitDeliveryRequest(@ModelAttribute(DeploymentAdvice.DEPLOYMENT_DOMAIN_NAME) String domainName, @ModelAttribute(DeploymentAdvice.DEPLOYMENT_SHORT_NAME) String deploymentShortName, @RequestBody VolunteerService.DeliveryForm request) {
    log.info("Received delivery request for site: {}", request.site);
    try {
      VolunteerService.VolunteerDeliveryRequest createdDelivery = volunteerService.createVolunteerDelivery(jdbi, request);

      // Build and send sms
      String updateMessage = String.format(
          "%s Supply Sites: " +
              "\n A delivery request to %s has been created. " +
              "\n Visit: %s%s to view delivery portal.",
          deploymentShortName,
          createdDelivery.getSiteName(),
          domainName, createdDelivery.getPortalURL());

      // todo: Send Text Notification to volunteer and site manager
      smsSender.send(createdDelivery.getCleanedSitePhoneNumber(),  updateMessage);
      smsSender.send(createdDelivery.getCleanedVolunteerPhoneNumber(),  updateMessage);

      return ResponseEntity.ok(createdDelivery.urlKey);
    } catch (Exception e) {
      log.error(e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to create delivery: " + e.getMessage());
    }
  }

  /**
   * Return Site items and information
   * */
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
   * Checks if
   * - delivery exists and
   * - if the user is already logged in check if user is a site manager or the volunteer
   * Returns
   * - the urlKey and
   * - a boolean representing if the user requires verification or not
   * */
  public static ModelAndView deliveryPortal(Jdbi jdbi, String userPhone, List<Long> userSites, String urlKey) {
    // Get Volunteer Delivery
    log.info("Received request for Volunteer Delivery {}", urlKey.toUpperCase());
    VolunteerService.VolunteerDeliveryRequest deliveryRequest = VolunteerService.getVolunteerDeliveryRequest(jdbi, urlKey.toUpperCase());

    Map<String, Object> pageParams = new HashMap<>();

    // If volunteer Delivery is not available reroute them to home
    // todo: create a 404 not found page to redirect to

    pageParams.put("urlKey", urlKey);

    // Check if user requires phone verification
    // true if user sites does not include siteId and user's phone number is not the volunteer's number
    pageParams.put(
        "userRequiresPhoneAuth",
        !userSites.contains(deliveryRequest.siteId) && !Objects.equals(deliveryRequest.volunteerPhone, userPhone)
    );

    pageParams.put("userPhone",  userPhone == null ? "" : userPhone);

    return new ModelAndView("volunteer/delivery/request", pageParams);
  }

  /**
   * Params: urlKey, phoneNumber, and volunteerSection
   * Verify that the provided phone number is associated with the delivery and
   * returns access level, delivery data, and provided phone number (used for auth later)
   * If not then an 401 error is returned
   * */
  @PostMapping("/volunteer/verify-delivery")
  ResponseEntity<?> verifyAndRetrieveDelivery(@RequestBody VolunteerService.VerificationRequest body) {

    // Check access
    VolunteerService.Access access = VolunteerService.verifyVolunteerPortalAccess(jdbi, body.urlKey, body.phoneNumber, "delivery");

    // If user does not hav access return 403 forbidden response
    if (!(access.isAuthorized()))  {
      log.info("Verification failed for volunteer volunteer delivery: {}", body.urlKey);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access denied: User is not verified");
    };

    VolunteerService.VolunteerDeliveryRequest deliveryRequest = VolunteerService.getVolunteerDeliveryRequest(jdbi, body.getUrlKey());

    // Only shows volunteer and manager phone numbers if the request status is accepted
    HashMap<String, Object> requestInfo = deliveryRequest.scrubDataBasedOnStatus();

    HashMap<String, Object> response = new HashMap<>();
    response.put("access", access);
    response.put("request", requestInfo);

    // Returning the phone number to use as verification when updating status
    response.put("userPhoneNumber", body.phoneNumber);

    return ResponseEntity.ok(response);
  }

  /**
   * Params: urlKey, new status, and phone number
   * Verify the user
   * Update the delivery
   * Returns the updated delivery
   * an error if not authorized to make the change
   */
  @PostMapping("/volunteer/delivery/update")
  ResponseEntity<?> updateDeliveryStatus(@ModelAttribute(DeploymentAdvice.DEPLOYMENT_DOMAIN_NAME) String domainName, @ModelAttribute(DeploymentAdvice.DEPLOYMENT_SHORT_NAME) String deploymentShortName, @RequestBody VolunteerService.UpdateRequest reqBody) {
    log.info("Received delivery update: {}", reqBody);

    // Check access
    VolunteerService.Access access = VolunteerService.verifyVolunteerPortalAccess(jdbi, reqBody.urlKey, reqBody.phoneNumber, "delivery");
    if (!access.isAuthorized()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body("User does not have authorization to update delivery");
    }

    // Check delivery exists
    VolunteerService.VolunteerDeliveryRequest deliveryRequest = VolunteerService.getVolunteerDeliveryRequest(jdbi, reqBody.getUrlKey());

    // update status
    VolunteerService.VolunteerDeliveryRequest updatedRequest = VolunteerService.updateDeliveryStatus(jdbi, access, reqBody.status ,deliveryRequest);

    // Build and send sms
    String updateMessage = String.format(
        "%s Supply Sites: " +
        "\n Delivery %s has been updated to %s. " +
        "\n Visit: %s%s to view delivery portal.",
        deploymentShortName,
        updatedRequest.getUrlKey(), updatedRequest.getStatus(),
        domainName, updatedRequest.getPortalURL());

    // todo: Send Text Notification to volunteer and site manager
    smsSender.send(updatedRequest.getCleanedSitePhoneNumber(),  updateMessage);
    smsSender.send(updatedRequest.getCleanedVolunteerPhoneNumber(),  updateMessage);

    HashMap <String, Object> response = new HashMap<>();
    response.put("request", updatedRequest.scrubDataBasedOnStatus());
    response.put("userPhoneNumber", reqBody.phoneNumber);
    return ResponseEntity.ok(response);
  }


}
