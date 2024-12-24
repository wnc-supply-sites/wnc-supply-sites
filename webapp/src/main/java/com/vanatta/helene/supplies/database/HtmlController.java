package com.vanatta.helene.supplies.database;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HtmlController {

  @GetMapping("/")
  public ModelAndView home(HttpServletRequest request) {//}, @ModelAttribute("loggedIn") boolean isLoggedIn) {
    Map<String,Object> params = new HashMap<>();
//    params.put("loggedIn", request.getSession().getAttribute("loggedIn"));
    return new ModelAndView("home/home", params);
  }
  
  @GetMapping("/log-out")
  public RedirectView logout(HttpServletResponse response) {
    Cookie cookie = new Cookie("auth", null);
    cookie.setMaxAge(0);
    cookie.setSecure(true);
    cookie.setHttpOnly(true);
    response.addCookie(cookie);
    return new RedirectView("/");
  }
}
