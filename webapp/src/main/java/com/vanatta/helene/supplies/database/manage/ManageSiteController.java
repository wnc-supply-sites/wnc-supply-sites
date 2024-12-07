package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.data.CountyDao;
import com.vanatta.helene.supplies.database.data.SiteType;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.util.EnumUtil;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class ManageSiteController {

  static final String PATH_SELECT_SITE = "/manage/select-site";
  private final Jdbi jdbi;
  private final SendSiteUpdate sendSiteUpdate;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SiteSelection {
    Long id;
    String name;
  }

  /** User will be shown a page to select the site they want to manage. */
  @GetMapping(PATH_SELECT_SITE)
  ModelAndView showSelectSitePage() {
    return showSelectSitePage(jdbi);
  }

  public static ModelAndView showSelectSitePage(Jdbi jdbi) {
    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put("sites", ManageSiteDao.fetchSiteList(jdbi));
    return new ModelAndView("manage/select-site", pageParams);
  }

  /**
   * After a site is selected, user selects which aspect they want to manage (eg: inventory, status)
   */
  @GetMapping("/manage/site-selected")
  ModelAndView showSiteSelectedPage(@RequestParam String siteId) {

    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      return showSelectSitePage();
    }

    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("siteName", siteName);
    pageParams.put("siteId", siteId);
    return new ModelAndView("manage/site-selected", pageParams);
  }

  /** Returns null if ID is not valid or DNE. */
  private String fetchSiteName(String siteId) {
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

  static final String COUNTY_LIST = "countyList";
  static final String STATE_LIST = "stateList";

  /** Fetches data for the manage site page */
  @GetMapping("/manage/contact")
  ModelAndView showSiteContactPage(String siteId) {
    Map<String, Object> pageParams = new HashMap<>();

    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      return showSelectSitePage();
    }

    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, Long.parseLong(siteId));
    if (data == null) {
      return new ModelAndView("redirect:" + PATH_SELECT_SITE);
    }

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

    Map<String, List<String>> counties = CountyDao.fetchFullCountyListing(jdbi);
    pageParams.put("fullCountyList", counties);
    pageParams.put(STATE_LIST, createItemListing(data.getState(), counties.keySet()));
    pageParams.put(COUNTY_LIST, createItemListing(data.getCounty(), counties.get(data.getState())));

    return new ModelAndView("manage/contact", pageParams);
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

  /** Displays the 'manage-status' page. */
  @GetMapping("/manage/status")
  ModelAndView showManageStatusPage(String siteId) {
    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      return showSelectSitePage();
    }

    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("siteName", siteName);
    pageParams.put("siteId", siteId);

    ManageSiteDao.SiteStatus siteStatus =
        ManageSiteDao.fetchSiteStatus(jdbi, Long.parseLong(siteId));
    pageParams.put("siteActive", siteStatus.isActive() ? "true" : null);
    pageParams.put("siteAcceptingDonations", siteStatus.isAcceptingDonations() ? "true" : null);
    pageParams.put(
        "siteDistributingDonations", siteStatus.isDistributingSupplies() ? "true" : null);

    pageParams.put(
        "distributionSiteChecked",
        siteStatus.getSiteTypeEnum() == SiteType.DISTRIBUTION_CENTER ? "checked" : "");
    pageParams.put(
        "supplyHubChecked", siteStatus.getSiteTypeEnum() == SiteType.SUPPLY_HUB ? "checked" : "");

    return new ModelAndView("manage/status", pageParams);
  }

  @AllArgsConstructor
  @Getter
  public enum EnumStatusUpdateFlag {
    ACTIVE("active"),
    SITE_TYPE("distSite"),
    ACCEPTING_SUPPLIES("acceptingSupplies"),
    DISTRIBUTING_SUPPLIES("distributingSupplies"),
    PUBLICLY_VISIBLE("publiclyVisible"),
    ;
    final String text;

    static Optional<EnumStatusUpdateFlag> fromText(String input) {
      return EnumUtil.mapText(values(), EnumStatusUpdateFlag::getText, input);
    }
  }

  /** REST endpoint to toggle the status of sites (active/accepting donations). */
  @PostMapping("/manage/update-status")
  @ResponseBody
  ResponseEntity<?> updateStatus(@RequestBody Map<String, String> params) {
    log.info("Update site status request received: {}", params);

    String siteId = params.get("siteId");
    String statusFlag = params.get("statusFlag");
    String newValue = params.get("newValue");

    String siteName = fetchSiteName(siteId);
    if (siteName == null) {
      return ResponseEntity.badRequest().body("Invalid site id: " + siteId);
    }

    if (newValue == null
        || !(newValue.equalsIgnoreCase("true") || newValue.equalsIgnoreCase("false"))) {
      return ResponseEntity.badRequest().body("Invalid new value: " + newValue);
    }

    log.info("Site update received, site name: {}, params; {}", siteName, params);

    var flag = EnumStatusUpdateFlag.fromText(statusFlag).orElse(null);
    if (flag == null) {
      log.warn("Status page, invalid status flag received. Params: {}", params);
      return ResponseEntity.badRequest().body("Invalid status flag: " + statusFlag);
    }

    switch (flag) {
      case ACCEPTING_SUPPLIES:
        ManageSiteDao.updateSiteAcceptingDonationsFlag(
            jdbi, Long.parseLong(siteId), Boolean.parseBoolean(newValue));
        break;
      case DISTRIBUTING_SUPPLIES:
        ManageSiteDao.updateSiteDistributingDonationsFlag(
            jdbi, Long.parseLong(siteId), Boolean.parseBoolean(newValue));
        break;
      case SITE_TYPE:
        var siteType =
            Boolean.parseBoolean(newValue) ? SiteType.DISTRIBUTION_CENTER : SiteType.SUPPLY_HUB;
        ManageSiteDao.updateSiteType(jdbi, Long.parseLong(siteId), siteType);
        break;
      case PUBLICLY_VISIBLE:
        ManageSiteDao.updateSitePubliclyVisible(
            jdbi, Long.parseLong(siteId), Boolean.parseBoolean(newValue));
        break;
      case ACTIVE:
        ManageSiteDao.updateSiteActiveFlag(
            jdbi, Long.parseLong(siteId), Boolean.parseBoolean(newValue));
        break;
      default:
        throw new IllegalArgumentException("Unmapped status flag: " + statusFlag);
    }

    sendSiteUpdate.sendFullUpdate(Long.parseLong(siteId));
    return ResponseEntity.ok().body("Updated");
  }
}
