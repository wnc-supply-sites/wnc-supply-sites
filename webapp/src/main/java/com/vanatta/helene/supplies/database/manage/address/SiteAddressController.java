package com.vanatta.helene.supplies.database.manage.address;

import com.vanatta.helene.supplies.database.data.CountyDao;
import com.vanatta.helene.supplies.database.manage.SelectSiteController;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import com.vanatta.helene.supplies.database.util.HtmlSelectOptionsUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class SiteAddressController {

  private final Jdbi jdbi;

  public static final String MANAGE_ADDRESS_PATH = "/manage/address/address";

  /** Fetches data for the manage site page */
  @GetMapping(MANAGE_ADDRESS_PATH)
  ModelAndView showSiteContactPage(String siteId) {
    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, Long.parseLong(siteId));
    if (data == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put(PageParam.SITE_ID.text, siteId);
    pageParams.put(PageParam.SITE_NAME.text, data.getSiteName());
    pageParams.put(PageParam.ADDRESS.text, data.getAddress());
    pageParams.put(PageParam.CITY.text, Optional.ofNullable(data.getCity()).orElse(""));
    pageParams.put(PageParam.WEBSITE.text, Optional.ofNullable(data.getWebsite()).orElse(""));
    pageParams.put(PageParam.FACEBOOK.text, Optional.ofNullable(data.getFacebook()).orElse(""));

    Map<String, List<String>> counties = CountyDao.fetchFullCountyListing(jdbi);
    pageParams.put(PageParam.FULL_COUNTY_LIST.text, counties);
    pageParams.put(
        PageParam.STATE_LIST.text,
        HtmlSelectOptionsUtil.createItemListing(data.getState(), counties.keySet()));
    pageParams.put(
        PageParam.COUNTY_LIST.text,
        HtmlSelectOptionsUtil.createItemListing(data.getCounty(), counties.get(data.getState())));

    return new ModelAndView("manage/adress/address", pageParams);
  }

  @AllArgsConstructor
  enum PageParam {
    SITE_ID("siteId"),
    SITE_NAME("siteName"),
    ADDRESS("address"),
    CITY("city"),
    WEBSITE("website"),
    FACEBOOK("facebook"),
    FULL_COUNTY_LIST("fullCountyList"),
    STATE_LIST("stateList"),
    COUNTY_LIST("countyList"),
    ;
    final String text;
  }
}
