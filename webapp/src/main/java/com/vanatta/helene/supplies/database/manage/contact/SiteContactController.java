package com.vanatta.helene.supplies.database.manage.contact;

import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import com.vanatta.helene.supplies.database.manage.SelectSiteController;
import com.vanatta.helene.supplies.database.manage.UserSiteAuthorization;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailDao;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
  ModelAndView showSiteContactPage(
      @ModelAttribute(LoggedInAdvice.USER_SITES) List<Long> sites, @RequestParam String siteId) {
    SiteDetailDao.SiteDetailData siteData =
        UserSiteAuthorization.isAuthorizedForSite(jdbi, sites, siteId).orElse(null);
    if (siteData == null) {
      return new ModelAndView("redirect:" + SelectSiteController.PATH_SELECT_SITE);
    }

    SiteDetailDao.SiteDetailData data = SiteDetailDao.lookupSiteById(jdbi, Long.parseLong(siteId));
    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put(PageParam.SITE_ID.text, siteId);
    pageParams.put(PageParam.SITE_NAME.text, data.getSiteName());
    pageParams.put(
        PageParam.SITE_CONTACT_NAME.text, Optional.ofNullable(data.getContactName()).orElse(""));
    pageParams.put(
        PageParam.SITE_CONTACT_NUMBER.text,
        Optional.ofNullable(data.getContactNumber()).orElse(""));
    pageParams.put(
        PageParam.ADDITIONAL_CONTACTS.text, ContactDao.getManagers(jdbi, Long.parseLong(siteId)));

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

  @PostMapping("/manage/remove-manager")
  ResponseEntity<String> removeManager(@RequestBody Map<String, String> params) {
    log.info("/manage/remove-manager received params: {}", params);

    long siteId = Long.parseLong(params.get("siteId"));
    Long managerId =
        Optional.ofNullable(params.get("managerId"))
            .map(s -> s.isBlank() ? null : s)
            .map(Long::parseLong)
            .orElse(null);

    ContactDao.removeAdditionalSiteManager(jdbi, siteId, managerId);

    return ResponseEntity.ok(
        """
        {"message": "removed"}
        """);
  }

  @PostMapping("/manage/add-manager")
  ResponseEntity<String> addManager(@RequestBody Map<String, String> params) {
    log.info("/manage/add-manager received params: {}", params);

    long siteId = Long.parseLong(params.get("siteId"));
    Long managerId =
        Optional.ofNullable(params.get("managerId"))
            .map(s -> s.isBlank() ? null : s)
            .map(Long::parseLong)
            .orElse(null);
    String contactName = params.get("name");
    String contactPhone = params.get("phone");

    final long idUpdated;
    if (managerId == null) {
      try {
        idUpdated = ContactDao.addAdditionalSiteManager(jdbi, siteId, contactName, contactPhone);
      } catch (Exception e) {
        if (e.getMessage().contains("duplicate key value")) {
          return ResponseEntity.badRequest()
              .body(
                  """
            {"error": "Duplicate phone number"}
            """);
        } else {
          log.error(
              "Error saving: siteId = {}, contact name = {}, contact phone ={}",
              siteId,
              contactName,
              contactPhone,
              e);
          return ResponseEntity.internalServerError()
              .body(
                  """
            {"error": "Database error saving data"}
            """);
        }
      }
    } else {
      var manager =
          ContactDao.SiteManager.builder()
              .id(managerId)
              .name(contactName)
              .phone(contactPhone)
              .build();
      ContactDao.updateAdditionalSiteManager(jdbi, siteId, manager);
      idUpdated = managerId;
    }

    return ResponseEntity.ok(
        String.format(
            """
        {"id": %s, "message": "%s"}
        """,
            idUpdated, managerId == null ? "Saved" : "Updated"));
  }
}
