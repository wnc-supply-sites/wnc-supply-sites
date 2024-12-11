package com.vanatta.helene.supplies.database.manage;

import com.vanatta.helene.supplies.database.export.update.SendSiteUpdate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class SelectSiteController {

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
    return ManageSiteDao.fetchSiteName(jdbi, siteId);
  }

  static final String COUNTY_LIST = "countyList";
  static final String STATE_LIST = "stateList";

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
}
