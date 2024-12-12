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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    Map<String, Object> pageParams = new HashMap<>();

    String siteName = ManageSiteDao.fetchSiteName(jdbi, siteId);
    if (siteName == null) {
      return SelectSiteController.showSelectSitePage(jdbi);
    }

    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, Long.parseLong(siteId));
    if (data == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
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
}
