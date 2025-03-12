package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.DeploymentAdvice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
@Slf4j
public class VolunteerController {
  private final Jdbi jdbi;

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
    String name;
    String address;
    String county;
    String state;
    List<Item> items;
  }

  /** Users will be shown a form to request to make a delivery */
  @GetMapping("/volunteer/delivery")
  ModelAndView deliveryForm(
      @ModelAttribute(DeploymentAdvice.DEPLOYMENT_STATE_LIST) List<String> states
  ) {
    return deliveryForm(jdbi, states);
  }

  public static ModelAndView deliveryForm(Jdbi jdbi, List<String> states) {
    Map<String, Object> pageParams = new HashMap<>();

    List<SiteSelect> sites = VolunteerDao.fetchSiteSelect(jdbi, states);

    pageParams.put("sites", sites);
    System.out.println(sites);
    return new ModelAndView("volunteer/delivery-form", pageParams);
  }

  /** Return json payload of site info */

  @GetMapping("/volunteer/site-items")
  ResponseEntity<?> getSiteItems(@RequestParam String siteId) {
    log.info("Get site info request received: {}", siteId);
    return ResponseEntity.ok().body("received");
  }



}
