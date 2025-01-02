package com.vanatta.helene.supplies.database.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminPortalController {

  @GetMapping("/admin")
  ModelAndView adminPortal() {
    return new ModelAndView("admin/portal");
  }
}
