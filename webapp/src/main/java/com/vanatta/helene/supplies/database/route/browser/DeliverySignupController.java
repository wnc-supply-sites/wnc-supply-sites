package com.vanatta.helene.supplies.database.route.browser;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@AllArgsConstructor
public class DeliverySignupController {

  private final Jdbi jdbi;

  enum TemplateParams {
    deliveryOptions
  }

  @GetMapping("/route/browser")
  ModelAndView routeBrowser() {

    List<DeliverySignupDao.DeliveryOption> deliveryOptions =
        DeliverySignupDao.findDeliveryOptions(jdbi).stream()
            .filter(RouteWeighting::filter)
            .sorted(Comparator.comparingDouble(DeliverySignupDao.DeliveryOption::sortScore))
            .toList();
    Map<String, Object> templateParams = new HashMap<>();
    templateParams.put(TemplateParams.deliveryOptions.name(), deliveryOptions);

    return new ModelAndView("driver/delivery-sign-up", templateParams);
  }
}
