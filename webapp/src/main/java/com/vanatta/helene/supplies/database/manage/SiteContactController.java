package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.data.CountyDao;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class SiteContactController {

  private final Jdbi jdbi;
  private final SendSiteUpdate sendSiteUpdate;

  static final String COUNTY_LIST = "countyList";
  static final String STATE_LIST = "stateList";
  public static final String PATH_MANAGE_CONTACTS = "/manage/contact";

  public static String buildManageContactsPath(long siteId) {
    return PATH_MANAGE_CONTACTS + "?siteId=" + siteId;
  }

  /** Fetches data for the manage site page */
  @GetMapping(PATH_MANAGE_CONTACTS)
  ModelAndView showSiteContactPage(String siteId) {
    Map<String, Object> pageParams =
        buildContactPageParams(jdbi, Long.parseLong(siteId)).orElse(null);
    if (pageParams == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    return new ModelAndView("manage/contact", pageParams);
  }

  // @VisibleForTesting
  static Optional<Map<String, Object>> buildContactPageParams(Jdbi jdbi, long siteId) {
    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, siteId);
    if (data == null) {
      return Optional.empty();
      //      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    /*
    String siteName = ManageSiteDao.fetchSiteName(jdbi, siteId);
    if (siteName == null) {
      return SelectSiteController.showSelectSitePage(jdbi);
    }
    */

    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put("siteId", siteId);
    pageParams.put("siteName", data.getSiteName());
    pageParams.put("address", data.getAddress());
    pageParams.put("city", Optional.ofNullable(data.getCity()).orElse(""));
    pageParams.put("website", Optional.ofNullable(data.getWebsite()).orElse(""));
    pageParams.put("facebook", Optional.ofNullable(data.getFacebook()).orElse(""));
    pageParams.put("hours", Optional.ofNullable(data.getHours()).orElse(""));
    pageParams.put("siteContactName", Optional.ofNullable(data.getContactName()).orElse(""));
    pageParams.put("siteContactEmail", Optional.ofNullable(data.getContactEmail()).orElse(""));
    pageParams.put("siteContactNumber", Optional.ofNullable(data.getContactNumber()).orElse(""));
    pageParams.put(
        "additionalContacts", Optional.ofNullable(data.getAdditionalContacts()).orElse(""));
    pageParams.put("onboarded", data.isOnboarded());
    pageParams.put("badNumbers", Optional.ofNullable(data.getBadNumbers()).orElse(""));

    Map<String, List<String>> counties = CountyDao.fetchFullCountyListing(jdbi);
    pageParams.put("fullCountyList", counties);
    pageParams.put(STATE_LIST, createItemListing(data.getState(), counties.keySet()));
    pageParams.put(COUNTY_LIST, createItemListing(data.getCounty(), counties.get(data.getState())));

    return Optional.of(pageParams);
  }

  public static List<ItemListing> createItemListing(
      String selectedValue, Collection<String> allValues) {
    return allValues.stream()
        .map(
            value ->
                ItemListing.builder()
                    .name(value)
                    .selected(value.equals(selectedValue) ? "selected" : "")
                    .build())
        .sorted(Comparator.comparing(ItemListing::getName))
        .toList();
  }

  @Builder
  @Data
  public static class ItemListing {
    String name;

    /** Should either be blank, or "selected" */
    String selected;
  }
  
  
  /** Info update for a site, eg: site-rename, site contact info changed. */
  @PostMapping("/manage/update-site")
  @ResponseBody
  ResponseEntity<?> updateSiteData(@RequestBody Map<String, String> params) {
    log.info("Update site data request received: {}", params);
    
    String siteId = params.get("siteId");
    String field = params.get("field");
    String newValue = params.get("newValue");
    
    if (newValue != null) {
      newValue = newValue.trim();
    }
    
    if (fetchSiteName(siteId) == null) {
      log.warn("invalid site id: {}, request: {}", siteId, params);
      return ResponseEntity.badRequest().body("Invalid site id");
    }
    
    var siteField = ManageSiteDao.SiteField.lookupField(field);
    ManageSiteDao.updateSiteField(jdbi, Long.parseLong(siteId), siteField, newValue);
    log.info("Site updated: {}", params);
    sendSiteUpdate.sendFullUpdate(Long.parseLong(siteId));
    
    return ResponseEntity.ok().body("Updated");
  }
  
  /** Returns null if ID is not valid or DNE. */
  private String fetchSiteName(String siteId) {
    return ManageSiteDao.fetchSiteName(jdbi, siteId);
  }
  
}
