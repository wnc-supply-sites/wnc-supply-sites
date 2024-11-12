package com.vanatta.helene.supplies.database;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HtmlController {

  @GetMapping("/")
  public String home() {
    return "home.html";
  }

  @GetMapping("/supplies")
  public String supplies() {
    return "supplies";
  }
}
