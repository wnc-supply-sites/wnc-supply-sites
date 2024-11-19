package com.vanatta.helene.supplies.database.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

  private final String validUser;
  private final String validPass;

  LoginController(@Value("${auth.user}") String user, @Value("${auth.pass}") String pass) {
    this.validUser = user;
    this.validPass = pass;
  }

  @GetMapping("/login")
  public ModelAndView login(@RequestParam String redirectUri) {
    Map<String, String> pageParams = new HashMap<>();
    pageParams.put("redirectUri", redirectUri);
    pageParams.put("errorMessage", "");
    return new ModelAndView("login", pageParams);
  }

  @PostMapping(
      path = "/doLogin",
      consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
  public ModelAndView doLogin(
      @RequestParam MultiValueMap<String, String> params, HttpServletResponse response) {
    String user = params.get("user").getFirst();
    String password = params.get("password").getFirst();
    String redirectUri = params.get("redirectUri").getFirst();

    if(validUser.equals(user) && validPass.equals(password)) {
      Cookie cookie = new Cookie("auth", AuthKey.AUTH_KEY);
      response.addCookie(cookie);
      cookie.setMaxAge(7 * 24 * 60 * 60); // expires in 7 days
      cookie.setSecure(true);
      cookie.setHttpOnly(true);
      return new ModelAndView("redirect:" + redirectUri);
    } else {
      Map<String, String> pageParams = new HashMap<>();
      pageParams.put("redirectUri", redirectUri);
      pageParams.put("errorMessage", "Invalid Login");
      return new ModelAndView("login", pageParams);
    }
  }
}
