package com.vanatta.helene.supplies.database.supplies.site.details;

import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.manage.ManageSiteController;
import com.vanatta.helene.supplies.database.manage.inventory.InventoryController;
import com.vanatta.helene.supplies.database.supplies.SuppliesController;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
@Slf4j
public class SiteDetailController {

  static final String PATH_SITE_DETAIL = "/supplies/site-detail";

  private final Jdbi jdbi;
  private final CookieAuthenticator cookieAuthenticator;

  public static String buildSiteLink(long siteId) {
    return PATH_SITE_DETAIL + "?id=" + siteId;
  }

  @GetMapping(PATH_SITE_DETAIL)
  public ModelAndView siteDetail(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long airtableId,
      @RequestParam(required = false) Long wssId,
      HttpServletRequest request) {
    if (id == null && airtableId == null && wssId == null) {
      return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
    }

    if (id == null) {
      if (airtableId != null) {
        id = SiteDetailDao.lookupSiteIdByAirtableId(jdbi, airtableId);
        if (id == null) {
          log.warn("Invalid airtable id received for site detail lookup: {}", airtableId);
          return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
        }
      }

      if (wssId != null) {
        id = SiteDetailDao.lookupSiteIdByWssId(jdbi, wssId);
        if (id == null) {
          log.warn("Invalid wss id received for site detail lookup: {}", wssId);
          return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
        }
      }
    }
    assert id != null;

    boolean isLoggedIn = cookieAuthenticator.isAuthenticated(request);
    SiteDetailDao.SiteDetailData siteDetailData = SiteDetailDao.lookupSiteById(jdbi, id);

    // if site not found, not accessible, or for logged in only users, then redirect
    if (siteDetailData == null
        || !siteDetailData.isActive()
        || (!isLoggedIn && !siteDetailData.isPubliclyVisible())) {
      return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
    }

    Map<String, Object> siteDetails = new HashMap<>();

    if (isLoggedIn) {
      List<NeedsMatchingDao.NeedsMatchingResult> needsMatching =
          NeedsMatchingDao.executeByInternalId(jdbi, id);
      siteDetails.put("needsMatching", needsMatching);
    }

    siteDetails.put("editContactLink", ManageSiteController.buildManageContactsPath(id));
    siteDetails.put("editInventoryLink", InventoryController.buildInventoryPath(id));

    siteDetails.put("loggedIn", cookieAuthenticator.isAuthenticated(request));
    siteDetails.put("siteName", siteDetailData.getSiteName());

    siteDetails.put(
        "website",
        siteDetailData.getWebsite() == null || siteDetailData.getWebsite().isBlank()
            ? null
            : new WebsiteLink(siteDetailData.getWebsite()));
    siteDetails.put(
        "facebook",
        siteDetailData.getFacebook() == null || siteDetailData.getFacebook().isBlank()
            ? null
            : new WebsiteLink(siteDetailData.getFacebook()));
    siteDetails.put(
        "hours",
        siteDetailData.getHours() == null || siteDetailData.getHours().isBlank()
            ? null
            : siteDetailData.getHours());

    siteDetails.put("addressLine1", siteDetailData.getAddress());
    siteDetails.put(
        "addressLine2",
        String.format("%s, %s", siteDetailData.getCity(), siteDetailData.getState()));
    siteDetails.put(
        "googleMapsAddress",
        String.format(
            "%s, %s, %s",
            urlEncode(siteDetailData.getAddress()),
            urlEncode(siteDetailData.getCity()),
            urlEncode(siteDetailData.getState())));

    if (isLoggedIn) {
      siteDetails.put(
          "contactName",
          siteDetailData.getContactName() == null || siteDetailData.getContactName().isBlank()
              ? null
              : siteDetailData.getContactName());
      siteDetails.put(
          "contactNumber",
          siteDetailData.getContactNumber() == null || siteDetailData.getContactNumber().isBlank()
              ? null
              : ContactHref.newTelephone(siteDetailData.getContactNumber()));
      siteDetails.put(
          "contactEmail",
          siteDetailData.getContactEmail() == null
              ? null
              : ContactHref.newMailTo(siteDetailData.getContactEmail()));

      siteDetails.put("additionalContacts", siteDetailData.getAdditionalContacts());
    }
    return new ModelAndView("supplies/site-detail", siteDetails);
  }

  @Getter
  static class WebsiteLink {
    private final String href;
    private final String title;

    WebsiteLink(String link) {
      if (link.endsWith("/")) {
        link = link.substring(0, link.length() - 1);
      }

      if (link.startsWith("http://")) {
        href = link;
        title = link.substring("http://".length());
      } else if (link.startsWith("https://")) {
        href = link;
        title = link.substring("https://".length());
      } else {
        href = "http://" + link;
        title = link;
      }
    }
  }

  @Getter
  static class ContactHref {
    private final String href;
    private final String title;

    static ContactHref newTelephone(String number) {
      return new ContactHref(number, "tel");
    }

    static ContactHref newMailTo(String email) {
      return new ContactHref(email, "mailTo");
    }

    private ContactHref(String number, String contactType) {
      if (number == null) {
        throw new NullPointerException(
            "number should not be null, do not create this object with null data.");
      }
      href = contactType + ":" + number;
      title = number;
    }
  }

  /** Does a quick URL encoding of a given value. */
  private static String urlEncode(String toEncode) {
    return URLEncoder.encode(toEncode, StandardCharsets.UTF_8);
  }
}
