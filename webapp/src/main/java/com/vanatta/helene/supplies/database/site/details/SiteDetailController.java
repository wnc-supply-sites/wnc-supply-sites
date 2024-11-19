package com.vanatta.helene.supplies.database.site.details;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class SiteDetailController {

  private final Jdbi jdbi;

  @GetMapping("/supplies/site-detail")
  public ModelAndView siteDetail(@RequestParam Long id) {
    if (id == null) {
      throw new IllegalArgumentException("No site id specified, missing 'id' parameter");
    }
    SiteDetailDao.SiteDetailData siteDetailData = SiteDetailDao.lookupSiteById(jdbi, id);

    Map<String, String> siteDetails = new HashMap<>();
    siteDetails.put("siteName", siteDetailData.getSiteName());
    siteDetails.put(
        "contactNumber",
        Optional.ofNullable(siteDetailData.getContactNumber()).orElse("none listed"));
    siteDetails.put("addressLine1", siteDetailData.getAddress());
    siteDetails.put(
        "addressLine2",
        String.format("%s, %s", siteDetailData.getCity(), siteDetailData.getState()));

    siteDetails.put(
        "googleMapsAddress",
        String.format(
            "%s, %s, %s, %s",
            encode(siteDetailData.getSiteName()),
            encode(siteDetailData.getAddress()),
            encode(siteDetailData.getCity()),
            encode(siteDetailData.getState())));

    return new ModelAndView("supplies/site-detail", siteDetails);
  }

  private static String encode(String toEncode) {
    return URLEncoder.encode(toEncode, StandardCharsets.UTF_8);
  }
}
