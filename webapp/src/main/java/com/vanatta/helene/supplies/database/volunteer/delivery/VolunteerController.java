package com.vanatta.helene.supplies.database.volunteer.delivery;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@AllArgsConstructor
public class VolunteerController {

  @GetMapping("/volunteer/delivery")
  public ModelAndView deliveryForm(
      HttpServletRequest request
  ) {
    Map<String, Object> params = new HashMap<>();
    return new ModelAndView("volunteer/delivery/deliveryForm", params);
  }
}
