package com.vanatta.helene.supplies.database.manage.contact;

import com.vanatta.helene.supplies.database.manage.SelectSiteController;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
import java.util.HashMap;
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
public class SiteContactController {

  private final Jdbi jdbi;

  public static final String PATH_MANAGE_CONTACTS = "/manage/contact/contact";

  public static String buildManageContactsPath(long siteId) {
    return PATH_MANAGE_CONTACTS + "?siteId=" + siteId;
  }

  /** Fetches data for the manage site page */
  @GetMapping(PATH_MANAGE_CONTACTS)
  ModelAndView showSiteContactPage(String siteId) {
    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, Long.parseLong(siteId));
    if (data == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put(PageParam.SITE_ID.text, siteId);
    pageParams.put(PageParam.SITE_NAME.text, data.getSiteName());
    pageParams.put(
        PageParam.SITE_CONTACT_NAME.text, Optional.ofNullable(data.getContactName()).orElse(""));
    pageParams.put(
        PageParam.SITE_CONTACT_NUMBER.text,
        Optional.ofNullable(data.getContactNumber()).orElse(""));
    pageParams.put(
        PageParam.ADDITIONAL_CONTACTS.text,
        Optional.ofNullable(data.getAdditionalContacts()).orElse(""));

    return new ModelAndView("manage/contact/contact", pageParams);
  }

  @AllArgsConstructor
  public enum PageParam {
    SITE_ID("siteId"),
    SITE_NAME("siteName"),
    SITE_CONTACT_NAME("siteContactName"),
    SITE_CONTACT_NUMBER("siteContactNumber"),
    ADDITIONAL_CONTACTS("additionalContacts"),
    ;
    final String text;
  }
}
