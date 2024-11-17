package com.vanatta.helene.supplies.database.manage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class ManageSiteController {

  private final Jdbi jdbi;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteSelection {
    Long id;
    String name;
  }

  /** User will be shown a page to select the site they want to manage. */
  @GetMapping("/manage/select-site")
  ModelAndView selectSite() {
    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put("sites", ManageSiteDao.fetchSiteList(jdbi));
    return new ModelAndView("manage/select-site", pageParams);
  }

  /**
   * After a site is selected, user selects which aspect they want to manage (eg: inventory, status)
   */
  @GetMapping("/manage/site-selected")
  ModelAndView siteSelected(
      //      @CookieValue(value = "auth") String auth,
      @RequestParam String siteId) {

    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      return selectSite();
    }

    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("siteName", siteName);
    pageParams.put("siteId", siteId);
    return new ModelAndView("manage/site-selected", pageParams);
  }

  /** Returns null if ID is not valid or DNE. */
  String fetchSiteName(String siteId) {
    if (siteId == null || siteId.isBlank()) {
      return null;
    }

    try {
      long id = Long.parseLong(siteId);
      return ManageSiteDao.fetchSiteName(jdbi, id);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @GetMapping("/manage/contact")
  ModelAndView manageContact(
      //      @CookieValue(value = "auth") String auth,
      String siteId) {
    Map<String, String> pageParams = new HashMap<>();

    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      return selectSite();
    }

    String contact =
        Optional.ofNullable(ManageSiteDao.fetchSiteContact(jdbi, Long.parseLong(siteId)))
            .orElse("");
    pageParams.put("siteId", siteId);
    pageParams.put("siteName", siteName);
    pageParams.put("siteContact", contact);

    return new ModelAndView("/manage/contact", pageParams);
  }

  @PostMapping("/manage/update-contact")
  @ResponseBody
  ResponseEntity<?> updateContact(@RequestParam String siteId, @RequestParam String contactNumber) {

    return ResponseEntity.ok().body("Updated");
  }

  @GetMapping("/manage/status")
  ModelAndView manageStatus(
      //      @CookieValue(value = "auth") String auth,
      String siteId) {
    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("siteName", "site-name");
    pageParams.put("siteId", "site-id");

    return new ModelAndView("/manage/status", pageParams);
  }

  @GetMapping("/manage/inventory")
  ModelAndView manageInventory(
      //      @CookieValue(value = "auth") String auth,
      String siteId) {
    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("siteName", "site-name");
    pageParams.put("siteId", "site-id");

    return new ModelAndView("/manage/inventory", pageParams);
  }
}
