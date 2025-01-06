package com.vanatta.helene.supplies.database.route.browser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
// @AllArgsConstructor
public class RouteBrowserController {

  private final Jdbi jdbi;
  private final String mapsApiKey;

  private static final int PAGE_SIZE = 5;

  enum TemplateParams {
    deliveryOptions,
    lowCount,
    highCount,
    resultCount,
    pageNumbers,
    apiKey,
    ;
  }

  RouteBrowserController(Jdbi jdbi, @Value("${google.maps.api.key}") String mapsApiKey) {
    this.jdbi = jdbi;
    this.mapsApiKey = mapsApiKey;
  }

  @GetMapping("/route/browser")
  ModelAndView routeBrowser(@RequestParam(required = false) Integer page) {
    if (page == null) {
      page = 1;
    }

    List<RouteBrowserDao.DeliveryOption> deliveryOptions =
        RouteBrowserDao.findDeliveryOptions(jdbi).stream()
            .filter(RouteWeighting::filter)
            .sorted(Comparator.comparingDouble(RouteBrowserDao.DeliveryOption::sortScore))
            .toList();
    Map<String, Object> templateParams = new HashMap<>();

    templateParams.put(TemplateParams.apiKey.name(), mapsApiKey);

    int lowCount = (page - 1) * PAGE_SIZE;
    int highCount = Math.min((page) * PAGE_SIZE, deliveryOptions.size());
    templateParams.put(TemplateParams.lowCount.name(), lowCount);
    templateParams.put(TemplateParams.highCount.name(), highCount);
    templateParams.put(TemplateParams.resultCount.name(), deliveryOptions.size());
    templateParams.put(
        TemplateParams.deliveryOptions.name(), deliveryOptions.subList(lowCount, highCount));

    List<PageNumber> pages = new ArrayList<>();
    int pageCount = (deliveryOptions.size() / PAGE_SIZE) + 1;
    for (int i = 1; i <= pageCount; i++) {
      pages.add(PageNumber.builder().number(i).build());
    }
    templateParams.put(TemplateParams.pageNumbers.name(), pages);

    return new ModelAndView("route/browser", templateParams);
  }

  @lombok.Value
  @Builder
  static class PageNumber {
    int number;
    String cssClasses = "";
  }
  /*
    {{resultCount}} Results, Result {{lowCount}} through {{highCount}}
  </div>
  {{#pageNumbers}}
  <a href="route/browser?page={{number}}" class="{{cssClasses}}">{{number}}</a>
  {{/pageNumbers}}

   */
}
