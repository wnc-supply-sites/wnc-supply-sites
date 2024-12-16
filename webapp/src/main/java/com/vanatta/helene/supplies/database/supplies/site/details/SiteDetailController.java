package com.vanatta.helene.supplies.database.supplies.site.details;

import com.vanatta.helene.supplies.database.auth.CookieAuthenticator;
import com.vanatta.helene.supplies.database.delivery.Delivery;
import com.vanatta.helene.supplies.database.delivery.DeliveryDao;
import com.vanatta.helene.supplies.database.manage.SiteContactController;
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

  @AllArgsConstructor
  enum TemplateParams {
    EDIT_CONTACT_LINK("editContactLink"),
    EDIT_INVENTORY_LINK("editInventoryLink"),
    LOGGED_IN("loggedIn"),
    SITE_NAME("siteName"),
    WEBSITE("website"),
    FACEBOOK("facebook"),
    HOURS("hours"),
    ADDRESS_LINE1("addressLine1"),
    ADDRESS_LINE2("addressLine2"),
    GOOGLE_MAPS_ADDRESS("googleMapsAddress"),
    HAS_FORK_LIFT("hasForklift"),
    HAS_LOADING_DOCK("hasLoadingDock"),
    HAS_INDOOR_STORAGE("hasIndoorStorage"),
    CONTACT_NAME("contactName"),
    CONTACT_NUMBER("contactNumber"),
    CONTACT_EMAIL("contactEmail"),
    ADDITIONAL_CONTACTS("additionalContacts"),
    NEEDS_MATCHING("needsMatching"),
    NEEDS_MATCH_COUNT("matchCount"),

    HAS_INCOMING_DELIVERIES("hasIncomingDeliveries"),
    INCOMING_DELIVERIES("incomingDeliveries"),

    HAS_OUTGOING_DELIVERIES("hasIncomingDeliveries"),
    OUTGOING_DELIVERIES("incomingDeliveries"),
    ;
    final String text;
  }

  @GetMapping(PATH_SITE_DETAIL)
  public ModelAndView siteDetail(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long airtableId,
      @RequestParam(required = false) Long wssId,
      HttpServletRequest request) {
    return siteDetail(id, airtableId, wssId, cookieAuthenticator.isAuthenticated(request));
  }

  // @VisibleForTesting
  public ModelAndView siteDetail(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) Long airtableId,
      @RequestParam(required = false) Long wssId,
      boolean isLoggedIn) {
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

    SiteDetailDao.SiteDetailData siteDetailData = SiteDetailDao.lookupSiteById(jdbi, id);

    // if site not found, not accessible, or for logged in only users, then redirect
    if (siteDetailData == null
        || !siteDetailData.isActive()
        || (!isLoggedIn && !siteDetailData.isPubliclyVisible())) {
      return new ModelAndView("redirect:" + SuppliesController.PATH_SUPPLY_SEARCH);
    }

    Map<String, Object> siteDetails = new HashMap<>();

    siteDetails.put(
        TemplateParams.EDIT_CONTACT_LINK.text, SiteContactController.buildManageContactsPath(id));
    siteDetails.put(
        TemplateParams.EDIT_INVENTORY_LINK.text, InventoryController.buildInventoryPath(id));
    siteDetails.put(TemplateParams.LOGGED_IN.text, isLoggedIn);
    siteDetails.put(TemplateParams.SITE_NAME.text, siteDetailData.getSiteName());

    siteDetails.put(
        TemplateParams.WEBSITE.text,
        siteDetailData.getWebsite() == null || siteDetailData.getWebsite().isBlank()
            ? null
            : new WebsiteLink(siteDetailData.getWebsite()));
    siteDetails.put(
        TemplateParams.FACEBOOK.text,
        siteDetailData.getFacebook() == null || siteDetailData.getFacebook().isBlank()
            ? null
            : new WebsiteLink(siteDetailData.getFacebook()));
    siteDetails.put(
        TemplateParams.HOURS.text,
        siteDetailData.getHours() == null || siteDetailData.getHours().isBlank()
            ? null
            : siteDetailData.getHours());

    siteDetails.put(TemplateParams.ADDRESS_LINE1.text, siteDetailData.getAddress());
    siteDetails.put(
        TemplateParams.ADDRESS_LINE2.text,
        String.format("%s, %s", siteDetailData.getCity(), siteDetailData.getState()));
    siteDetails.put(
        TemplateParams.GOOGLE_MAPS_ADDRESS.text,
        String.format(
            "%s, %s, %s",
            urlEncode(siteDetailData.getAddress()),
            urlEncode(siteDetailData.getCity()),
            urlEncode(siteDetailData.getState())));

    if (isLoggedIn) {
      siteDetails.put(TemplateParams.HAS_FORK_LIFT.text, siteDetailData.isHasForklift());
      siteDetails.put(TemplateParams.HAS_LOADING_DOCK.text, siteDetailData.isHasLoadingDock());
      siteDetails.put(TemplateParams.HAS_INDOOR_STORAGE.text, siteDetailData.isHasIndoorStorage());

      siteDetails.put(
          TemplateParams.CONTACT_NAME.text,
          siteDetailData.getContactName() == null || siteDetailData.getContactName().isBlank()
              ? null
              : siteDetailData.getContactName());
      siteDetails.put(
          TemplateParams.CONTACT_NUMBER.text,
          siteDetailData.getContactNumber() == null || siteDetailData.getContactNumber().isBlank()
              ? null
              : ContactHref.newTelephone(siteDetailData.getContactNumber()));
      siteDetails.put(
          TemplateParams.CONTACT_EMAIL.text,
          siteDetailData.getContactEmail() == null
              ? null
              : ContactHref.newMailTo(siteDetailData.getContactEmail()));

      siteDetails.put(
          TemplateParams.ADDITIONAL_CONTACTS.text, siteDetailData.getAdditionalContacts());

      List<Delivery> allDeliveries = DeliveryDao.fetchDeliveriesBySiteId(jdbi, id);

      List<Delivery> incomingDeliveries =
          allDeliveries.stream()
              .filter(d -> d.getToSite().equals(siteDetailData.getSiteName()))
              .toList();
      siteDetails.put(TemplateParams.HAS_INCOMING_DELIVERIES.text, !incomingDeliveries.isEmpty());
      siteDetails.put(TemplateParams.INCOMING_DELIVERIES.text, incomingDeliveries);

      List<Delivery> outgoingDeliveries =
          allDeliveries.stream()
              .filter(d -> !d.getToSite().equals(siteDetailData.getSiteName()))
              .toList();
      siteDetails.put(TemplateParams.HAS_OUTGOING_DELIVERIES.text, !outgoingDeliveries.isEmpty());
      siteDetails.put(TemplateParams.OUTGOING_DELIVERIES.text, outgoingDeliveries);

      List<NeedsMatchingDao.NeedsMatchingResult> needsMatching =
          NeedsMatchingDao.executeByInternalId(jdbi, id);
      siteDetails.put(TemplateParams.NEEDS_MATCHING.text, needsMatching);
      siteDetails.put(TemplateParams.NEEDS_MATCH_COUNT.text, needsMatching.size());
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
