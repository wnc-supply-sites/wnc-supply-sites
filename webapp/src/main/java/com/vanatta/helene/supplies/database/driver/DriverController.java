package com.vanatta.helene.supplies.database.driver;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
@AllArgsConstructor
public class DriverController {

  private final Jdbi jdbi;

  @GetMapping("/driver/portal")
  ModelAndView showDriverPortal() {

    return new ModelAndView("driver/portal");
  }
}
