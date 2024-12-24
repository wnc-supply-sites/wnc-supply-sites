package com.vanatta.helene.supplies.database.manage.contact;

import com.vanatta.helene.supplies.database.data.CountyDao;
import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.manage.SelectSiteController;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.Arrays;
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

  public static final String PATH_MANAGE_CONTACTS = "/manage/contact/contact";

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

    return new ModelAndView("manage/contact/contact", pageParams);
  }

  // @VisibleForTesting
  static Optional<Map<String, Object>> buildContactPageParams(Jdbi jdbi, long siteId) {
    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, siteId);
    if (data == null) {
      return Optional.empty();
    }

    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put(PageParam.SITE_ID.text, siteId);
    pageParams.put(PageParam.SITE_NAME.text, data.getSiteName());
    pageParams.put(
        PageParam.SITE_CONTACT_NAME.text, Optional.ofNullable(data.getContactName()).orElse(""));
    pageParams.put(
        PageParam.SITE_CONTACT_EMAIL.text, Optional.ofNullable(data.getContactEmail()).orElse(""));
    pageParams.put(
        PageParam.SITE_CONTACT_NUMBER.text,
        Optional.ofNullable(data.getContactNumber()).orElse(""));
    pageParams.put(
        PageParam.ADDITIONAL_CONTACTS.text,
        Optional.ofNullable(data.getAdditionalContacts()).orElse(""));


    return Optional.of(pageParams);
  }

  @AllArgsConstructor
  enum PageParam {
    SITE_ID("siteId"),
    SITE_NAME("siteName"),
    SITE_CONTACT_NAME("siteContactName"),
    SITE_CONTACT_EMAIL("siteContactEmail"),
    SITE_CONTACT_NUMBER("siteContactNumber"),
    ADDITIONAL_CONTACTS("additionalContacts"),
    ;
    final String text;
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

    if (ManageSiteDao.fetchSiteName(jdbi, Long.parseLong(siteId)) == null) {
      log.warn("invalid site id: {}, params: {}", siteId, params);
      return ResponseEntity.badRequest().body("Invalid site id");
    }

    var siteField = ManageSiteDao.SiteField.lookupField(field).orElse(null);
    if (siteField == null) {
      log.warn("Invalid field requested for update: {}, params: {}", field, params);
      return ResponseEntity.badRequest().body("Invalid field: " + field);
    }
    ManageSiteDao.updateSiteField(jdbi, Long.parseLong(siteId), siteField, newValue);
    log.info("Site updated: {}", params);
    sendSiteUpdate.sendFullUpdate(Long.parseLong(siteId));

    return ResponseEntity.ok().body("Updated");
  }
  
  @AllArgsConstructor
  enum SiteReceivingParam {
    SITE_ID("siteId"),
    HAS_FORKLIFT("hasForkLift"),
    HAS_LOADING_DOCK("hasLoadingDock"),
    HAS_INDOOR_STORAGE("hasIndoorStorage");

    final String text;
  }

  @PostMapping("/manage/update-site-receiving")
  ResponseEntity<?> updateSiteReceiving(@RequestBody Map<String, String> params) {
    log.info("Update site receiving request received: {}", params);
    boolean hasAllData =
        params
            .keySet()
            .containsAll(Arrays.stream(SiteReceivingParam.values()).map(v -> v.text).toList());
    if (!hasAllData) {
      log.warn("Bad request received to update site data, missing input. Received: {}", params);
      return ResponseEntity.badRequest().body("Bad request - missing data");
    }

    long siteId = Long.parseLong(params.get(SiteReceivingParam.SITE_ID.text));

    var capabilities =
        ManageSiteDao.ReceivingCapabilities.builder()
            .forklift(Boolean.parseBoolean(params.get(SiteReceivingParam.HAS_FORKLIFT.text)))
            .loadingDock(Boolean.parseBoolean(params.get(SiteReceivingParam.HAS_LOADING_DOCK.text)))
            .indoorStorage(
                Boolean.parseBoolean(params.get(SiteReceivingParam.HAS_INDOOR_STORAGE.text)))
            .build();

    ManageSiteDao.updateReceivingCapabilities(jdbi, siteId, capabilities);

    sendSiteUpdate.sendFullUpdate(siteId);
    return ResponseEntity.ok().body("updated");
  }
}
