package com.vanatta.helene.supplies.database.driver;

import com.vanatta.helene.supplies.database.supplies.site.details.NeedsMatchingDao;
import com.vanatta.helene.supplies.database.supplies.site.details.SiteDetailController;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class DeliverySignupController {
  
  private final Jdbi jdbi;
  
  enum TemplateParams {
    deliveryOptions
  }
  
  @GetMapping("/driver/delivery-sign-up")
  ModelAndView deliverySignUp() {

    List<DeliverySignupDao.DeliveryOption> deliveryOptions =
        DeliverySignupDao.findDeliveryOptions(jdbi);
    Map<String, Object> templateParams = new HashMap<>();
    templateParams.put(TemplateParams.deliveryOptions.name(), deliveryOptions);
    
    return new ModelAndView("driver/delivery-sign-up", templateParams);
  }
  
}
