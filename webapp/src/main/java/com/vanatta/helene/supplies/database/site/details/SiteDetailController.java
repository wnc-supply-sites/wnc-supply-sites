package com.vanatta.helene.supplies.database.site.details;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
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

  private final Jdbi jdbi;
  private final CookieAuthenticator cookieAuthenticator;

  @GetMapping("/supplies/site-detail")
  public ModelAndView siteDetail(@RequestParam Long id, HttpServletRequest request) {
    if (id == null) {
      throw new IllegalArgumentException("No site id specified, missing 'id' parameter");
    }
    SiteDetailDao.SiteDetailData siteDetailData = SiteDetailDao.lookupSiteById(jdbi, id);

    Map<String, Object> siteDetails = new HashMap<>();

    siteDetails.put("loggedIn", cookieAuthenticator.isAuthenticated(request));

    siteDetails.put("siteName", siteDetailData.getSiteName());
    siteDetails.put(
        "website",
        Optional.ofNullable(siteDetailData.getWebsite()).map(WebsiteLink::new).orElse(null));
    siteDetails.put("contactNumber", new ContactNumber(siteDetailData.getContactNumber()));
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

    return new ModelAndView("supplies/site-detail", siteDetails);
  }

  @Getter
  static class WebsiteLink {
    private final String href;
    private final String title;

    WebsiteLink(String link) {
      if(link.endsWith("/")) {
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
  static class ContactNumber {
    private final String href;
    private final String title;

    ContactNumber(String number) {
      if (number == null) {
        href = null;
        title = "None listed";
      } else {
        href = "tel:" + number;
        title = number;
      }
    }
  }

  /** Does a quick URL encoding of a given value. */
  private static String urlEncode(String toEncode) {
    return URLEncoder.encode(toEncode, StandardCharsets.UTF_8);
  }
}
