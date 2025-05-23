package com.vanatta.helene.supplies.database.browse.routes;

import com.vanatta.helene.supplies.database.DeploymentAdvice;
import com.vanatta.helene.supplies.database.auth.LoggedInAdvice;
import com.vanatta.helene.supplies.database.auth.UserRole;
import com.vanatta.helene.supplies.database.data.CountyDao;
import com.vanatta.helene.supplies.database.supplies.filters.AuthenticatedMode;
import com.vanatta.helene.supplies.database.util.HtmlSelectOptionsUtil;
import com.vanatta.helene.supplies.database.util.PhoneNumberUtil;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BrowseRoutesController {

  private final Jdbi jdbi;
  private final String mapsApiKey;

  static final int PAGE_SIZE = 5;

  public static final String BROWSE_ROUTES_PATH = "/browse/routes";

  enum TemplateParams {
    deliveryOptions,
    hasDeliveries,
    hasPaging,
    resultCount,
    pageNumbers,
    apiKey,
    siteList,
    countyList,
    currentPage,
    currentSite,
    currentCounty,
    currentPagePath,
    isDriver,
    fromVolunteerDates,
    toVolunteerDates,
    ;
  }

  BrowseRoutesController(Jdbi jdbi, @Value("${google.maps.api.key}") String mapsApiKey) {
    this.jdbi = jdbi;
    this.mapsApiKey = mapsApiKey;
  }

  @GetMapping(BROWSE_ROUTES_PATH)
  ModelAndView browseRoutes(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) String siteWssId,
      @RequestParam(required = false) String county,
      @ModelAttribute(LoggedInAdvice.USER_ROLES) List<UserRole> userRoles,
      @ModelAttribute(DeploymentAdvice.DEPLOYMENT_STATE_LIST) List<String> stateList) {

    if (page == null) {
      page = 1;
    } else {
      page = Math.max(1, page);
    }

    final long siteWssIdCleaned =
        (siteWssId == null
                || siteWssId.isBlank()
                || PhoneNumberUtil.removeNonNumeric(siteWssId).isBlank())
            ? 0L
            : Long.parseLong(siteWssId);

    Map<String, Object> templateParams = new HashMap<>();

    List<String> counties = new ArrayList<>();
    counties.add("");
    counties.addAll(
        CountyDao.fetchActiveCountyList(jdbi, AuthenticatedMode.AUTHENTICATED, stateList));
    templateParams.put(
        TemplateParams.countyList.name(),
        HtmlSelectOptionsUtil.createItemListingWithFuzzyStartsWith(county, counties));
    String currentCounty =
        (county == null || county.isBlank())
            ? null
            : counties.stream().filter(c -> c.startsWith(county)).findAny().orElse(null);

    List<DeliveryOption> deliveryOptions =
        BrowseRoutesDao.findDeliveryOptions(jdbi, siteWssIdCleaned, currentCounty, stateList)
            .stream()
            .filter(RouteWeighting::filter)
            .sorted(Comparator.comparingDouble(DeliveryOption::sortScore))
            .toList();
    int pageCount = (int) Math.ceil(((double) deliveryOptions.size()) / PAGE_SIZE);
    page = Math.min(page, pageCount);

    templateParams.put(TemplateParams.currentSite.name(), siteWssIdCleaned);
    templateParams.put(TemplateParams.currentCounty.name(), Optional.ofNullable(county).orElse(""));
    templateParams.put(TemplateParams.currentPagePath.name(), BROWSE_ROUTES_PATH);
    templateParams.put(TemplateParams.hasDeliveries.name(), !deliveryOptions.isEmpty());
    templateParams.put(TemplateParams.hasPaging.name(), pageCount > 1);

    List<String> volunteerDates = getVolunteerDays(LocalDate.now(ZoneId.of("America/New_York")));
    templateParams.put(TemplateParams.fromVolunteerDates.name(), volunteerDates);
    templateParams.put(TemplateParams.toVolunteerDates.name(), volunteerDates);

    templateParams.put(TemplateParams.isDriver.name(), userRoles.contains(UserRole.DRIVER));
    templateParams.put(TemplateParams.apiKey.name(), mapsApiKey);
    templateParams.put(TemplateParams.currentPage.name(), page);
    templateParams.put(TemplateParams.resultCount.name(), deliveryOptions.size());

    if (page > 0) {
      int lowCount = (page - 1) * PAGE_SIZE;
      int highCount = Math.min((page) * PAGE_SIZE, deliveryOptions.size());
      templateParams.put(
          TemplateParams.deliveryOptions.name(), deliveryOptions.subList(lowCount, highCount));
    } else {
      templateParams.put(TemplateParams.deliveryOptions.name(), null);
    }

    List<Site> sites = new ArrayList<>();
    sites.add(Site.BLANK);

    sites.addAll(
        BrowseRoutesDao.fetchSites(jdbi, stateList).stream()
            .map(s -> s.getWssId() == siteWssIdCleaned ? s.toBuilder().selected(true).build() : s)
            .toList());
    templateParams.put(TemplateParams.siteList.name(), sites);

    List<PageNumber> pages = new ArrayList<>();
    for (int i = 1; i <= pageCount; i++) {
      pages.add(PageNumber.builder().number(i).cssClasses(i == page ? "current-page" : "").build());
    }
    templateParams.put(TemplateParams.pageNumbers.name(), pages);

    return new ModelAndView("browse/routes", templateParams);
  }

  @lombok.Value
  @Builder
  static class PageNumber {
    int number;
    @Builder.Default String cssClasses = "";
  }

  @Builder(toBuilder = true)
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class Site {
    static Site BLANK = Site.builder().siteName("").wssId(0L).build();
    Long wssId;
    String siteName;
    Boolean selected;
  }

  static List<String> getVolunteerDays(LocalDate fromDate) {
    List<String> days = new ArrayList<>();
    for (long i = 0L; i < 15L; i++) {
      days.add(fromDate.plusDays(i).format(DateTimeFormatter.ofPattern("MMM-dd")));
    }
    return days;
  }
}
