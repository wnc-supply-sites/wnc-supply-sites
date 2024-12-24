package com.vanatta.helene.supplies.database.manage.receiving;

import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import com.vanatta.helene.supplies.database.manage.ManageSiteDao;
import com.vanatta.helene.supplies.database.manage.SelectSiteController;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.util.HtmlSelectOptionsUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class SiteReceivingController {

  private final Jdbi jdbi;
  private final SendSiteUpdate sendSiteUpdate;

  /** Fetches data for the manage site page */
  @GetMapping("/manage/receiving/receiving")
  ModelAndView showSiteContactPage(String siteId) {
    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, Long.parseLong(siteId));
    if (data == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put(PageParam.SITE_ID.text, siteId);
    pageParams.put(PageParam.SITE_NAME.text, data.getSiteName());
    pageParams.put(PageParam.HOURS.text, Optional.ofNullable(data.getHours()).orElse(""));

    pageParams.put(PageParam.HAS_FORKLIST.text, data.isHasForklift());
    pageParams.put(PageParam.HAS_INDOOR_STORAGE.text, data.isHasIndoorStorage());
    pageParams.put(PageParam.HAS_LOADING_DOCK.text, data.isHasLoadingDock());
    pageParams.put(PageParam.RECEIVING_NOTES.text, data.getReceivingNotes());

    List<HtmlSelectOptionsUtil.ItemListing> maxSupplyOptions =
        HtmlSelectOptionsUtil.createItemListing(
            data.getMaxSupply(),
            ManageSiteDao.getAllMaxSupplyOptions(jdbi).stream()
                .map(ManageSiteDao.MaxSupplyOption::getName)
                .toList());
    pageParams.put(PageParam.MAX_SUPPLY_OPTIONS.text, maxSupplyOptions);

    return new ModelAndView("manage/receiving/receiving", pageParams);
  }

  @AllArgsConstructor
  enum PageParam {
    SITE_ID("siteId"),
    SITE_NAME("siteName"),
    HOURS("hours"),
    MAX_SUPPLY_OPTIONS("maxSupplyDeliveryOptions"),
    HAS_FORKLIST("hasForklift"),
    HAS_LOADING_DOCK("hasLoadingDock"),
    HAS_INDOOR_STORAGE("hasIndoorStorage"),
    RECEIVING_NOTES("receivingNotes"),
    ;
    final String text;
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
