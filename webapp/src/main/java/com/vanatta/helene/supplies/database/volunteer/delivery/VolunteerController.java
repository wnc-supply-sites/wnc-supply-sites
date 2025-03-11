package com.vanatta.helene.supplies.database.volunteer.delivery;

import com.vanatta.helene.supplies.database.DeploymentAdvice;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
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

  public static ModelAndView deliveryForm(Jdbi jdbi, List<String> states) {
    Map<String, Object> pageParams = new HashMap<>();
    pageParams.put("state", states);
    return new ModelAndView("volunteer/delivery/deliveryForm", pageParams);
  }





}
