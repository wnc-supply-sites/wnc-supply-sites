package com.vanatta.helene.supplies.database.supplies.site.details;

import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.supplies.SuppliesController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class SiteDetailController {

  static final String PATH_SITE_DETAIL = "/supplies/site-detail";

  private final Jdbi jdbi;
  private final CookieAuthenticator cookieAuthenticator;

  @GetMapping(PATH_SITE_DETAIL)
  public ModelAndView siteDetail(
      @RequestParam Long id, HttpServletRequest request, HttpServletResponse response) {
    if (id == null) {
      return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
    }
    boolean isLoggedIn = cookieAuthenticator.isAuthenticated(request);
    SiteDetailDao.SiteDetailData siteDetailData = SiteDetailDao.lookupSiteById(jdbi, id);

    // if site not found, not accessible, or for logged in only users, then redirect
    if (siteDetailData == null
        || !siteDetailData.isActive()
        || (!isLoggedIn && !siteDetailData.isPubliclyVisible())) {
      return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
    }

    Map<String, Object> siteDetails = new HashMap<>();

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
            : new WebsiteLink(siteDetailData.getFacebook()));

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
