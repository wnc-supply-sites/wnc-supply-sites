package com.vanatta.helene.supplies.database.route.browser;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
//@AllArgsConstructor
public class RouteBrowserController {

  private final Jdbi jdbi;
  private final String mapsApiKey;
  

  enum TemplateParams {
    deliveryOptions,
    resultCount,
    apiKey,
    ;
  }

  RouteBrowserController(Jdbi jdbi,
                         @Value("${google.maps.api.key}") String mapsApiKey
                           ) {
    this.jdbi = jdbi;
    this.mapsApiKey = mapsApiKey;
    
  }
  
  @GetMapping("/route/browser")
  ModelAndView routeBrowser(@RequestParam(required = false) Integer pageNumber) {

    List<RouteBrowserDao.DeliveryOption> deliveryOptions =
        RouteBrowserDao.findDeliveryOptions(jdbi).stream()
            .filter(RouteWeighting::filter)
            .sorted(Comparator.comparingDouble(RouteBrowserDao.DeliveryOption::sortScore))
            .toList();
    Map<String, Object> templateParams = new HashMap<>();

    templateParams.put(TemplateParams.deliveryOptions.name(), deliveryOptions.subList(0, 5));

    templateParams.put(TemplateParams.resultCount.name(), deliveryOptions.size());

    templateParams.put(TemplateParams.apiKey.name(), mapsApiKey);
    
    
    return new ModelAndView("route/browser", templateParams);
  }
}
