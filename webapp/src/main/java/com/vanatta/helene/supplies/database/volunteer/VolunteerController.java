package com.vanatta.helene.supplies.database.volunteer;

import com.vanatta.helene.supplies.database.DeploymentAdvice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class VolunteerController {
  private final Jdbi jdbi;

  /** Users will be shown a form to request to make a delivery */
  @GetMapping("/volunteer/delivery")
  ModelAndView deliveryForm(
      @ModelAttribute(DeploymentAdvice.DEPLOYMENT_STATE_LIST) List<String> states
  ) {
    return deliveryForm(jdbi, states);
  }

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Site {
    Long id;
    String name;
    String countyName;
    String state;
  }

  public static ModelAndView deliveryForm(Jdbi jdbi, List<String> states) {
    Map<String, Object> pageParams = new HashMap<>();

    List<Site> sites = VolunteerDao.fetchSites(jdbi, states);

    pageParams.put("sites", sites);

    return new ModelAndView("volunteer/delivery-form", pageParams);
  }

}
